package com.company.mysapbtpsdkproject.service

import android.content.Context
import android.util.Base64
import androidx.work.*
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_Entities
import com.company.mysapbtpsdkproject.app.SAPWizardApplication
import com.sap.cloud.mobile.flowv2.core.FlowContextRegistry
import com.sap.cloud.mobile.flowv2.securestore.UserSecureStoreDelegate
import com.sap.cloud.mobile.foundation.common.ClientProvider
import com.sap.cloud.mobile.foundation.common.SettingsProvider
import com.sap.cloud.mobile.foundation.model.AppConfig
import com.sap.cloud.mobile.odata.core.AndroidSystem
import com.sap.cloud.mobile.odata.core.LoggerFactory
import com.sap.cloud.mobile.odata.offline.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.net.URL
import java.security.SecureRandom
import java.util.*

object OfflineWorkerUtil {
    var offlineODataProvider: OfflineODataProvider? = null
        private set

    /** OData service for interacting with local OData Provider */
    lateinit var zCIMS_INT_SRV_Entities: ZCIMS_INT_SRV_Entities
        private set
    private val logger = LoggerFactory.getLogger(OfflineWorkerUtil::class.java.toString())
    private val progressListeners = mutableSetOf<OfflineProgressListener>()

    var userSwitchFlag = false

    var openRequest: OneTimeWorkRequest? = null
        private set

    @JvmStatic
    fun resetOpenRequest() {
        openRequest = null
    }

    var syncRequest: OneTimeWorkRequest? = null
        private set

    @JvmStatic
    fun resetSyncRequest() {
        syncRequest = null
    }

    const val OFFLINE_OPEN_WORKER_UNIQUE_NAME = "offline_init_sync_worker"
    const val OFFLINE_SYNC_WORKER_UNIQUE_NAME = "offline_sync_worker"

    const val OUTPUT_ERROR_KEY = "output.error"
    const val OUTPUT_ERROR_DETAIL = "output.error.detail"

    /** Name of the offline data file on the application file space */
    private const val OFFLINE_DATASTORE = "OfflineDataStore"
    const val OFFLINE_DATASTORE_ENCRYPTION_KEY =
        "Offline_DataStore_EncryptionKey"

    /** Header name for application version */
    private const val APP_VERSION_HEADER = "X-APP-VERSION"

    /** Connection ID of Mobile Application*/
    private const val CONNECTION_ID_ZCIMS_INT_SRV_ENTITIES = "com.sap.crfid.cRFID_Demo"

    /** The preference to say whether offline is initialized. */
    const val PREF_OFFLINE_INITIALIZED = "pref.offline.db.initialized"

    /** The preference to say whether app is in foreground service. */
    const val PREF_FOREGROUND_SERVICE = "pref.foreground.service"

    /** The preference to say whether app just deleted registration. */
    const val PREF_DELETE_REGISTRATION = "pref.delete.registration"

    @JvmStatic
    fun addProgressListener(listener: OfflineProgressListener) =
        progressListeners.add(listener)

    @JvmStatic
    fun removeProgressListener(listener: OfflineProgressListener) =
        progressListeners.remove(listener)

    @JvmStatic
    fun resetOfflineODataProvider() {
        offlineODataProvider = null
    }

    private val delegate = object : OfflineODataProviderDelegate {
        override fun updateOpenProgress(
            p0: OfflineODataProvider,
            p1: OfflineODataProviderOperationProgress
        ) = notifyListeners(p0, p1)

        override fun updateDownloadProgress(
            p0: OfflineODataProvider,
            p1: OfflineODataProviderDownloadProgress
        ) = notifyListeners(p0, p1)

        override fun updateUploadProgress(
            p0: OfflineODataProvider,
            p1: OfflineODataProviderOperationProgress
        ) = notifyListeners(p0, p1)

        override fun updateFailedRequest(p0: OfflineODataProvider, p1: OfflineODataFailedRequest) =
            Unit

        override fun updateSendStoreProgress(
            p0: OfflineODataProvider,
            p1: OfflineODataProviderOperationProgress
        ) = notifyListeners(p0, p1)

        private fun notifyListeners(
            p0: OfflineODataProvider,
            p1: OfflineODataProviderOperationProgress
        ) {
            logger.debug("Progress " + p1.currentStepNumber + " out of " + p1.totalNumberOfSteps)
            MainScope().launch {
                progressListeners.forEach {
                    it.onOfflineProgress(p0, p1)
                }
            }
        }
    }

    /*
     * Create OfflineODataProvider
     * This is a blocking call, no data will be transferred until open, download, upload
     */
    @JvmStatic
    fun initializeOffline(
        context: Context,
        appConfig: AppConfig,
        runtimeMultipleUserMode: Boolean
    ) {
        if (offlineODataProvider != null) return
        if (FlowContextRegistry.flowContext.getCurrentUserId().isNullOrEmpty())
            error("Current user not ready yet.")

        AndroidSystem.setContext(context as SAPWizardApplication)
        val serviceUrl = appConfig.serviceUrl
        try {
            val url = URL(serviceUrl + CONNECTION_ID_ZCIMS_INT_SRV_ENTITIES)
            val offlineODataParameters = OfflineODataParameters().apply {
                isEnableRepeatableRequests = true
                storeName = OFFLINE_DATASTORE
                currentUser = FlowContextRegistry.flowContext.getCurrentUserId()
                isForceUploadOnUserSwitch = runtimeMultipleUserMode
                val encryptionKey = if (runtimeMultipleUserMode) {
                    UserSecureStoreDelegate.getInstance().getOfflineEncryptionKey()
                } else { //If is single user mode, create and save a key into user secure store for accessing offline DB
                    if (UserSecureStoreDelegate.getInstance().getData<String>(OFFLINE_DATASTORE_ENCRYPTION_KEY) == null) {
                        val bytes = ByteArray(32)
                        val random = SecureRandom()
                        random.nextBytes(bytes)
                        val key = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        UserSecureStoreDelegate.getInstance().saveData(OFFLINE_DATASTORE_ENCRYPTION_KEY, key)
                        Arrays.fill(bytes, 0.toByte())
                        key
                    } else {
                        UserSecureStoreDelegate.getInstance().getData<String>(OFFLINE_DATASTORE_ENCRYPTION_KEY)
                    }
                }
                storeEncryptionKey = encryptionKey
            }.also {
                // Set the default application version
                val customHeaders = it.customHeaders
                customHeaders[APP_VERSION_HEADER] = SettingsProvider.get().applicationVersion
                // In case of offlineODataParameters.customHeaders returning a new object if customHeaders from offlineODataParameters is null, set again as below
                it.setCustomHeaders(customHeaders)
            }

            offlineODataProvider = OfflineODataProvider(
                url,
                offlineODataParameters,
                ClientProvider.get(),
                delegate
            ).apply {
                val iNPOHEADERSetQuery = OfflineODataDefiningQuery("IN_PO_HEADERSet", "IN_PO_HEADERSet", false)
                addDefiningQuery(iNPOHEADERSetQuery)
                val iNPOITEMSetQuery = OfflineODataDefiningQuery("IN_PO_ITEMSet", "IN_PO_ITEMSet", false)
                addDefiningQuery(iNPOITEMSetQuery)
                val iNPOSetQuery = OfflineODataDefiningQuery("IN_POSet", "IN_POSet", false)
                addDefiningQuery(iNPOSetQuery)
                val iNPOStLocSetQuery = OfflineODataDefiningQuery("IN_PO_StLocSet", "IN_PO_StLocSet", false)
                addDefiningQuery(iNPOStLocSetQuery)
                val iNPRINTMEDIASetQuery = OfflineODataDefiningQuery("IN_PRINT_MEDIASet", "IN_PRINT_MEDIASet", true)
                addDefiningQuery(iNPRINTMEDIASetQuery)
                val iNPRINTPOITEMSetQuery = OfflineODataDefiningQuery("IN_PRINT_PO_ITEMSet", "IN_PRINT_PO_ITEMSet", false)
                addDefiningQuery(iNPRINTPOITEMSetQuery)
                val iNPRINTPOSetQuery = OfflineODataDefiningQuery("IN_PRINT_POSet", "IN_PRINT_POSet", false)
                addDefiningQuery(iNPRINTPOSetQuery)
                val iNREPRINTPOSetQuery = OfflineODataDefiningQuery("IN_REPRINT_POSet", "IN_REPRINT_POSet", false)
                addDefiningQuery(iNREPRINTPOSetQuery)
                val iNREPRINTRSNSetQuery = OfflineODataDefiningQuery("IN_REPRINT_RSNSet", "IN_REPRINT_RSNSet", false)
                addDefiningQuery(iNREPRINTRSNSetQuery)
                val iNSTOCKOVWSetQuery = OfflineODataDefiningQuery("IN_STOCK_OVWSet", "IN_STOCK_OVWSet", false)
                addDefiningQuery(iNSTOCKOVWSetQuery)
                val iNTMattomatSeSetQuery = OfflineODataDefiningQuery("INT_mattomat_seSet", "INT_mattomat_seSet", false)
                addDefiningQuery(iNTMattomatSeSetQuery)
                val iNTMattomatSetQuery = OfflineODataDefiningQuery("INT_mattomatSet", "INT_mattomatSet", false)
                addDefiningQuery(iNTMattomatSetQuery)
                val iNTMtomChargvalidSetQuery = OfflineODataDefiningQuery("INT_mtom_chargvalidSet", "INT_mtom_chargvalidSet", false)
                addDefiningQuery(iNTMtomChargvalidSetQuery)
                val intPhyInvPostSetQuery = OfflineODataDefiningQuery("Int_phy_inv_postSet", "Int_phy_inv_postSet", false)
                addDefiningQuery(intPhyInvPostSetQuery)
                val intPhyInvSetQuery = OfflineODataDefiningQuery("Int_phy_invSet", "Int_phy_invSet", false)
                addDefiningQuery(intPhyInvSetQuery)
                val intPhyValidSetQuery = OfflineODataDefiningQuery("Int_phy_validSet", "Int_phy_validSet", false)
                addDefiningQuery(intPhyValidSetQuery)
                val intSlocToSlocSetQuery = OfflineODataDefiningQuery("Int_Sloc_To_SlocSet", "Int_Sloc_To_SlocSet", false)
                addDefiningQuery(intSlocToSlocSetQuery)
                val intSlocValidSetQuery = OfflineODataDefiningQuery("Int_Sloc_validSet", "Int_Sloc_validSet", false)
                addDefiningQuery(intSlocValidSetQuery)
                val loginSrvSetQuery = OfflineODataDefiningQuery("Login_SrvSet", "Login_SrvSet", false)
                addDefiningQuery(loginSrvSetQuery)
                val matDetailsSetQuery = OfflineODataDefiningQuery("Mat_detailsSet", "Mat_detailsSet", false)
                addDefiningQuery(matDetailsSetQuery)
                val oUTGISCRAPSetQuery = OfflineODataDefiningQuery("OUT_GI_SCRAPSet", "OUT_GI_SCRAPSet", false)
                addDefiningQuery(oUTGISCRAPSetQuery)
                val oUTGISOSetQuery = OfflineODataDefiningQuery("OUT_GI_SOSet", "OUT_GI_SOSet", false)
                addDefiningQuery(oUTGISOSetQuery)
                val oUTPICKING1SetQuery = OfflineODataDefiningQuery("OUT_PICKING1Set", "OUT_PICKING1Set", false)
                addDefiningQuery(oUTPICKING1SetQuery)
                val oUTPICKINGSetQuery = OfflineODataDefiningQuery("OUT_PICKINGSet", "OUT_PICKINGSet", false)
                addDefiningQuery(oUTPICKINGSetQuery)
                val outGIReservationSetQuery = OfflineODataDefiningQuery("Out_GI_reservationSet", "Out_GI_reservationSet", false)
                addDefiningQuery(outGIReservationSetQuery)
                val rFMATTAGSetQuery = OfflineODataDefiningQuery("RF_MATTAGSet", "RF_MATTAGSet", false)
                addDefiningQuery(rFMATTAGSetQuery)
                zCIMS_INT_SRV_Entities = ZCIMS_INT_SRV_Entities(this)
            }
        } catch (e: Exception) {
            logger.error("Exception encountered setting up offline store: " + e.message)
        }
    }

    /*
     * Close and remove offline data store
     */
    @JvmStatic
    fun resetOffline(context: Context) {
        try {
            AndroidSystem.setContext(context)
            offlineODataProvider?.close()
            OfflineODataProvider.clear(OFFLINE_DATASTORE)
        } catch (e: OfflineODataException) {
            logger.error("Unable to reset Offline Data Store. Encountered exception: " + e.message)
        } finally {
            offlineODataProvider = null
        }
        progressListeners.clear()
    }

    @JvmStatic
    fun open(context: Context) {
        if (FlowContextRegistry.flowContext.getCurrentUserId() == null) {
            error("Current user not ready yet.")
        }

        if (!userSwitchFlag && openRequest != null) {
            return
        }

        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .build()

        openRequest = OneTimeWorkRequestBuilder<OfflineOpenWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            OFFLINE_OPEN_WORKER_UNIQUE_NAME,
            ExistingWorkPolicy.KEEP,
            openRequest!!
        )
    }

    @JvmStatic
    fun sync(context: Context) {
        syncRequest?.let {
            return
        }

        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .build()

        syncRequest = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            OFFLINE_SYNC_WORKER_UNIQUE_NAME,
            ExistingWorkPolicy.KEEP,
            syncRequest!!
        )
    }
}


abstract class OfflineProgressListener() {
    enum class WorkerType{
        OPEN, SYNC
    }

    private var previousStep = 0
    val totalStepsForTwoProgresses = 40

    fun onOfflineProgress(
        provider: OfflineODataProvider,
        progress: OfflineODataProviderOperationProgress
    ) {
        if (progress.currentStepNumber > previousStep) {
            previousStep = progress.currentStepNumber
            if (workerType == WorkerType.OPEN && !OfflineWorkerUtil.userSwitchFlag) {
                updateProgress(progress.currentStepNumber, progress.totalNumberOfSteps)
            } else {
                /*
                 * The half of totalStepsForTwoProgresses is for first progress, the other half is for second progress.
                 * To make two progresses as one progress, the current step number needs to be calculated.
                 * For example, totalStepsForTwoProgresses is 40, then first progress will proceed from step 0 to step 20, and the second one will proceed from step 20 to step 40.
                 * So getStartPoint will be 0 for the first progress and 20 for the second progress.
                 * If first progress completes by 20% (i.e. getCurrentStepNumber / getTotalNumberOfSteps = 20%), the overall progress will be 4/40.
                 * If second progress completes by 20%, the overall progress will be 24/40.
                 */
                val currentStepNumber = totalStepsForTwoProgresses / 2 * progress.currentStepNumber / progress.totalNumberOfSteps + getStartPoint()
                updateProgress(currentStepNumber, totalStepsForTwoProgresses)
            }
        }
        if (progress.currentStepNumber == progress.totalNumberOfSteps) {
            previousStep = 0
        }
    }

    abstract fun updateProgress(currentStep: Int, totalSteps: Int)
    abstract fun getStartPoint(): Int
    abstract val workerType: WorkerType
}
