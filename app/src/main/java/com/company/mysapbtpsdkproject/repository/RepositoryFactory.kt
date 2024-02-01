package com.company.mysapbtpsdkproject.repository

import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntitySets
import com.sap.cloud.android.odata.zcims_int_srv_entities.InPoHeader
import com.sap.cloud.android.odata.zcims_int_srv_entities.InPoItem
import com.sap.cloud.android.odata.zcims_int_srv_entities.InPo
import com.sap.cloud.android.odata.zcims_int_srv_entities.INPOStLoc
import com.sap.cloud.android.odata.zcims_int_srv_entities.InPrintMedia
import com.sap.cloud.android.odata.zcims_int_srv_entities.InPrintPoItem
import com.sap.cloud.android.odata.zcims_int_srv_entities.InPrintPo
import com.sap.cloud.android.odata.zcims_int_srv_entities.InReprintPo
import com.sap.cloud.android.odata.zcims_int_srv_entities.InReprintRsn
import com.sap.cloud.android.odata.zcims_int_srv_entities.InStockOvw
import com.sap.cloud.android.odata.zcims_int_srv_entities.INTMattomatSe
import com.sap.cloud.android.odata.zcims_int_srv_entities.INTMattomat
import com.sap.cloud.android.odata.zcims_int_srv_entities.INTMtomChargvalid
import com.sap.cloud.android.odata.zcims_int_srv_entities.IntPhyInvPost
import com.sap.cloud.android.odata.zcims_int_srv_entities.IntPhyInv
import com.sap.cloud.android.odata.zcims_int_srv_entities.IntPhyValid
import com.sap.cloud.android.odata.zcims_int_srv_entities.IntSlocToSloc
import com.sap.cloud.android.odata.zcims_int_srv_entities.IntSlocValid
import com.sap.cloud.android.odata.zcims_int_srv_entities.LoginSrv
import com.sap.cloud.android.odata.zcims_int_srv_entities.MatDetails
import com.sap.cloud.android.odata.zcims_int_srv_entities.OutGiScrap
import com.sap.cloud.android.odata.zcims_int_srv_entities.OutGiSo
import com.sap.cloud.android.odata.zcims_int_srv_entities.OutPicking1
import com.sap.cloud.android.odata.zcims_int_srv_entities.OutPicking
import com.sap.cloud.android.odata.zcims_int_srv_entities.OutGIReservation
import com.sap.cloud.android.odata.zcims_int_srv_entities.RfMattag

import com.sap.cloud.mobile.odata.EntitySet
import com.sap.cloud.mobile.odata.EntityValue
import com.sap.cloud.mobile.odata.Property
import com.company.mysapbtpsdkproject.service.OfflineWorkerUtil

import java.util.WeakHashMap

/*
 * Repository factory to construct repository for an entity set
 */
class RepositoryFactory
/**
 * Construct a RepositoryFactory instance. There should only be one repository factory and used
 * throughout the life of the application to avoid caching entities multiple times.
 */
{
    private val repositories: WeakHashMap<String, Repository<out EntityValue>> = WeakHashMap()

    /**
     * Construct or return an existing repository for the specified entity set
     * @param entitySet - entity set for which the repository is to be returned
     * @param orderByProperty - if specified, collection will be sorted ascending with this property
     * @return a repository for the entity set
     */
    fun getRepository(entitySet: EntitySet, orderByProperty: Property?): Repository<out EntityValue> {
        val zCIMS_INT_SRV_Entities = OfflineWorkerUtil.zCIMS_INT_SRV_Entities
        val key = entitySet.localName
        var repository: Repository<out EntityValue>? = repositories[key]
        if (repository == null) {
            repository = when (key) {
                EntitySets.inPOHEADERSet.localName -> Repository<InPoHeader>(zCIMS_INT_SRV_Entities, EntitySets.inPOHEADERSet, orderByProperty)
                EntitySets.inPOITEMSet.localName -> Repository<InPoItem>(zCIMS_INT_SRV_Entities, EntitySets.inPOITEMSet, orderByProperty)
                EntitySets.inPOSet.localName -> Repository<InPo>(zCIMS_INT_SRV_Entities, EntitySets.inPOSet, orderByProperty)
                EntitySets.inPOStLocSet.localName -> Repository<INPOStLoc>(zCIMS_INT_SRV_Entities, EntitySets.inPOStLocSet, orderByProperty)
                EntitySets.inPRINTMEDIASet.localName -> Repository<InPrintMedia>(zCIMS_INT_SRV_Entities, EntitySets.inPRINTMEDIASet, orderByProperty)
                EntitySets.inPRINTPOITEMSet.localName -> Repository<InPrintPoItem>(zCIMS_INT_SRV_Entities, EntitySets.inPRINTPOITEMSet, orderByProperty)
                EntitySets.inPRINTPOSet.localName -> Repository<InPrintPo>(zCIMS_INT_SRV_Entities, EntitySets.inPRINTPOSet, orderByProperty)
                EntitySets.inREPRINTPOSet.localName -> Repository<InReprintPo>(zCIMS_INT_SRV_Entities, EntitySets.inREPRINTPOSet, orderByProperty)
                EntitySets.inREPRINTRSNSet.localName -> Repository<InReprintRsn>(zCIMS_INT_SRV_Entities, EntitySets.inREPRINTRSNSet, orderByProperty)
                EntitySets.inSTOCKOVWSet.localName -> Repository<InStockOvw>(zCIMS_INT_SRV_Entities, EntitySets.inSTOCKOVWSet, orderByProperty)
                EntitySets.intMattomatSeSet.localName -> Repository<INTMattomatSe>(zCIMS_INT_SRV_Entities, EntitySets.intMattomatSeSet, orderByProperty)
                EntitySets.intMattomatSet.localName -> Repository<INTMattomat>(zCIMS_INT_SRV_Entities, EntitySets.intMattomatSet, orderByProperty)
                EntitySets.intMtomChargvalidSet.localName -> Repository<INTMtomChargvalid>(zCIMS_INT_SRV_Entities, EntitySets.intMtomChargvalidSet, orderByProperty)
                EntitySets.intPhyInvPostSet.localName -> Repository<IntPhyInvPost>(zCIMS_INT_SRV_Entities, EntitySets.intPhyInvPostSet, orderByProperty)
                EntitySets.intPhyInvSet.localName -> Repository<IntPhyInv>(zCIMS_INT_SRV_Entities, EntitySets.intPhyInvSet, orderByProperty)
                EntitySets.intPhyValidSet.localName -> Repository<IntPhyValid>(zCIMS_INT_SRV_Entities, EntitySets.intPhyValidSet, orderByProperty)
                EntitySets.intSlocToSlocSet.localName -> Repository<IntSlocToSloc>(zCIMS_INT_SRV_Entities, EntitySets.intSlocToSlocSet, orderByProperty)
                EntitySets.intSlocValidSet.localName -> Repository<IntSlocValid>(zCIMS_INT_SRV_Entities, EntitySets.intSlocValidSet, orderByProperty)
                EntitySets.loginSrvSet.localName -> Repository<LoginSrv>(zCIMS_INT_SRV_Entities, EntitySets.loginSrvSet, orderByProperty)
                EntitySets.matDetailsSet.localName -> Repository<MatDetails>(zCIMS_INT_SRV_Entities, EntitySets.matDetailsSet, orderByProperty)
                EntitySets.outGISCRAPSet.localName -> Repository<OutGiScrap>(zCIMS_INT_SRV_Entities, EntitySets.outGISCRAPSet, orderByProperty)
                EntitySets.outGISOSet.localName -> Repository<OutGiSo>(zCIMS_INT_SRV_Entities, EntitySets.outGISOSet, orderByProperty)
                EntitySets.outPICKING1Set.localName -> Repository<OutPicking1>(zCIMS_INT_SRV_Entities, EntitySets.outPICKING1Set, orderByProperty)
                EntitySets.outPICKINGSet.localName -> Repository<OutPicking>(zCIMS_INT_SRV_Entities, EntitySets.outPICKINGSet, orderByProperty)
                EntitySets.outGIReservationSet.localName -> Repository<OutGIReservation>(zCIMS_INT_SRV_Entities, EntitySets.outGIReservationSet, orderByProperty)
                EntitySets.rfMATTAGSet.localName -> Repository<RfMattag>(zCIMS_INT_SRV_Entities, EntitySets.rfMATTAGSet, orderByProperty)
                else -> throw AssertionError("Fatal error, entity set[$key] missing in generated code")
            }
            repositories[key] = repository
        }
        return repository
    }

    /**
     * Get rid of all cached repositories
     */
    fun reset() {
        repositories.clear()
    }
}
