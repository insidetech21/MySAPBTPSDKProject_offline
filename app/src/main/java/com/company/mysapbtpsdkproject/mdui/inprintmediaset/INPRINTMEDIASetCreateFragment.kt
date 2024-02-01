package com.company.mysapbtpsdkproject.mdui.inprintmediaset

import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import com.company.mysapbtpsdkproject.R
import com.company.mysapbtpsdkproject.databinding.FragmentInprintmediasetCreateBinding
import com.company.mysapbtpsdkproject.mdui.BundleKeys
import com.company.mysapbtpsdkproject.mdui.InterfacedFragment
import com.company.mysapbtpsdkproject.mdui.UIConstants
import com.company.mysapbtpsdkproject.repository.OperationResult
import com.company.mysapbtpsdkproject.viewmodel.inprintmedia.InPrintMediaViewModel
import com.sap.cloud.android.odata.zcims_int_srv_entities.InPrintMedia
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntityTypes
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntitySets
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader
import com.sap.cloud.mobile.odata.Property
import org.slf4j.LoggerFactory
import com.sap.cloud.mobile.odata.ByteStream
import com.sap.cloud.mobile.odata.StreamBase

/**
 * A fragment that is used for both update and create for users to enter values for the properties. When used for
 * update, an instance of the entity is required. In the case of create, a new instance of the entity with defaults will
 * be created. The default values may not be acceptable for the OData service.
 * This fragment is either contained in a [INPRINTMEDIASetListActivity] in two-pane mode (on tablets) or a
 * [INPRINTMEDIASetDetailActivity] on handsets.
 *
 * Arguments: Operation: [OP_CREATE | OP_UPDATE]
 *            InPrintMedia if Operation is update
 */
class INPRINTMEDIASetCreateFragment : InterfacedFragment<InPrintMedia, FragmentInprintmediasetCreateBinding>() {

    /** InPrintMedia object and it's copy: the modifications are done on the copied object. */
    private lateinit var inPrintMediaEntity: InPrintMedia
    private lateinit var inPrintMediaEntityCopy: InPrintMedia

    /** Indicate what operation to be performed */
    private lateinit var operation: String

    /** inPrintMediaEntity ViewModel */
    private lateinit var viewModel: InPrintMediaViewModel

    /** The update menu item */
    private lateinit var updateMenuItem: MenuItem

    /*
     * Get a default media resource when creating a media linked entity
     * Since it is a package resource, exception when converting to byte array is not expected
     * and not handled
     */
    private val defaultMediaResource: StreamBase
        get() {
            val inputStream = resources.openRawResource(R.raw.blank)
            val byteStream = ByteStream.fromInput(inputStream)
            byteStream.mediaType = "image/png"
            return byteStream
        }

    private val isInPrintMediaValid: Boolean
        get() {
            var isValid = true
            fragmentBinding.createUpdateInprintmedia.let { linearLayout ->
                for (i in 0 until linearLayout.childCount) {
                    val simplePropertyFormCell = linearLayout.getChildAt(i) as SimplePropertyFormCell
                    val propertyName = simplePropertyFormCell.tag as String
                    val property = EntityTypes.inPrintMedia.getProperty(propertyName)
                    val value = simplePropertyFormCell.value.toString()
                    if (!isValidProperty(property, value)) {
                        simplePropertyFormCell.setTag(R.id.TAG_HAS_MANDATORY_ERROR, true)
                        val errorMessage = resources.getString(R.string.mandatory_warning)
                        simplePropertyFormCell.isErrorEnabled = true
                        simplePropertyFormCell.error = errorMessage
                        isValid = false
                    } else {
                        if (simplePropertyFormCell.isErrorEnabled) {
                            val hasMandatoryError = simplePropertyFormCell.getTag(R.id.TAG_HAS_MANDATORY_ERROR) as Boolean
                            if (!hasMandatoryError) {
                                isValid = false
                            } else {
                                simplePropertyFormCell.isErrorEnabled = false
                            }
                        }
                        simplePropertyFormCell.setTag(R.id.TAG_HAS_MANDATORY_ERROR, false)
                    }
                }
            }
            return isValid
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_edit_options

        arguments?.let {
            (it.getString(BundleKeys.OPERATION))?.let { operationType ->
                operation = operationType
                activityTitle = when (operationType) {
                    UIConstants.OP_CREATE -> resources.getString(R.string.title_create_fragment, EntityTypes.inPrintMedia.localName)
                    else -> resources.getString(R.string.title_update_fragment) + " " + EntityTypes.inPrintMedia.localName

                }
            }
        }

        activity?.let {
            (it as INPRINTMEDIASetActivity).isNavigationDisabled = true
            viewModel = ViewModelProvider(it)[InPrintMediaViewModel::class.java]
            viewModel.createResult.observe(this) { result -> onComplete(result) }
            viewModel.updateResult.observe(this) { result -> onComplete(result) }

            inPrintMediaEntity = if (operation == UIConstants.OP_CREATE) {
                createInPrintMedia()
            } else {
                viewModel.selectedEntity.value!!
            }

            val workingCopy = when{ (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) -> {
                    savedInstanceState?.getParcelable<InPrintMedia>(KEY_WORKING_COPY, InPrintMedia::class.java)
                } else -> @Suppress("DEPRECATION") savedInstanceState?.getParcelable<InPrintMedia>(KEY_WORKING_COPY)
            }

            if (workingCopy == null) {
                inPrintMediaEntityCopy = inPrintMediaEntity.copy()
                inPrintMediaEntityCopy.entityTag = inPrintMediaEntity.entityTag
                inPrintMediaEntityCopy.oldEntity = inPrintMediaEntity
                inPrintMediaEntityCopy.editLink = inPrintMediaEntity.editLink
            } else {
                inPrintMediaEntityCopy = workingCopy
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        currentActivity.findViewById<ObjectHeader>(R.id.objectHeader)?.let {
            it.visibility = View.GONE
        }
        fragmentBinding.inPrintMedia = inPrintMediaEntityCopy
        return fragmentBinding.root
    }

    override  fun  initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentInprintmediasetCreateBinding.inflate(inflater, container, false)

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.save_item -> {
                updateMenuItem = menuItem
                enableUpdateMenuItem(false)
                onSaveItem()
            }
            else -> super.onMenuItemSelected(menuItem)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(secondaryToolbar != null) secondaryToolbar!!.title = activityTitle else activity?.title = activityTitle
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_WORKING_COPY, inPrintMediaEntityCopy)
        super.onSaveInstanceState(outState)
    }

    /** Enables the update menu item based on [enable] */
    private fun enableUpdateMenuItem(enable : Boolean = true) {
        updateMenuItem.also {
            it.isEnabled = enable
            it.icon?.alpha = if(enable) 255 else 130
        }
    }

    /** Saves the entity */
    private fun onSaveItem(): Boolean {
        if (!isInPrintMediaValid) {
            return false
        }
        (currentActivity as INPRINTMEDIASetActivity).isNavigationDisabled = false
        progressBar?.visibility = View.VISIBLE
        when (operation) {
            UIConstants.OP_CREATE -> {
                if (EntitySets.inPRINTMEDIASet.entityType.isMedia) {
                    val media = defaultMediaResource
                    viewModel.create(inPrintMediaEntityCopy, media)
                } else {
                    viewModel.create(inPrintMediaEntityCopy)
                }
            }
            UIConstants.OP_UPDATE -> viewModel.update(inPrintMediaEntityCopy)
        }
        return true
    }

    /**
     * Create a new InPrintMedia instance and initialize properties to its default values
     * Nullable property will remain null
     * For offline, keys will be unset to avoid collision should more than one is created locally
     * @return new InPrintMedia instance
     */
    private fun createInPrintMedia(): InPrintMedia {
        val entity = InPrintMedia(true)
        entity.unsetDataValue(InPrintMedia.mblnr)
        entity.unsetDataValue(InPrintMedia.ebeln)
        entity.unsetDataValue(InPrintMedia.ebelp)
        entity.unsetDataValue(InPrintMedia.matnr)
        entity.unsetDataValue(InPrintMedia.charg)
        entity.unsetDataValue(InPrintMedia.mcod1)
        entity.unsetDataValue(InPrintMedia.maktx)
        entity.unsetDataValue(InPrintMedia.meins)
        entity.unsetDataValue(InPrintMedia.lenum)
        return entity
    }

    /** Callback function to complete processing when updateResult or createResult events fired */
    private fun onComplete(result: OperationResult<InPrintMedia>) {
        progressBar?.visibility = View.INVISIBLE
        enableUpdateMenuItem(true)
        if (result.error != null) {
            (currentActivity as INPRINTMEDIASetActivity).isNavigationDisabled = true
            handleError(result)
        } else {
            if (operation == UIConstants.OP_UPDATE && !currentActivity.resources.getBoolean(R.bool.two_pane)) {
                viewModel.selectedEntity.value = inPrintMediaEntityCopy
            }
            if (currentActivity.resources.getBoolean(R.bool.two_pane)) {
                val listFragment = currentActivity.supportFragmentManager.findFragmentByTag(UIConstants.LIST_FRAGMENT_TAG)
                (listFragment as INPRINTMEDIASetListFragment).refreshListData()
            }
            (currentActivity as INPRINTMEDIASetActivity).onBackPressedDispatcher.onBackPressed()
        }
    }

    /** Simple validation: checks the presence of mandatory fields. */
    private fun isValidProperty(property: Property, value: String): Boolean {
        return !(!property.isNullable && value.isEmpty())
    }

    /**
     * Notify user of error encountered while execution the operation
     *
     * @param [result] operation result with error
     */
    private fun handleError(result: OperationResult<InPrintMedia>) {
        val errorMessage = when (result.operation) {
            OperationResult.Operation.UPDATE -> getString(R.string.update_failed_detail)
            OperationResult.Operation.CREATE -> getString(R.string.create_failed_detail)
            else -> throw AssertionError()
        }
        showError(errorMessage)
    }


    companion object {
        private val KEY_WORKING_COPY = "WORKING_COPY"
        private val LOGGER = LoggerFactory.getLogger(INPRINTMEDIASetActivity::class.java)
    }
}
