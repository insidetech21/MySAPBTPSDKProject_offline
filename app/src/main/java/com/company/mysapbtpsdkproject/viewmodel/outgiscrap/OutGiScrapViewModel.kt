package com.company.mysapbtpsdkproject.viewmodel.outgiscrap

import android.app.Application
import android.os.Parcelable

import com.company.mysapbtpsdkproject.viewmodel.EntityViewModel
import com.sap.cloud.android.odata.zcims_int_srv_entities.OutGiScrap
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntitySets

/*
 * Represents View model for OutGiScrap
 *
 * Having an entity view model for each <T> allows the ViewModelProvider to cache and return the view model of that
 * type. This is because the ViewModelStore of ViewModelProvider cannot not be able to tell the difference between
 * EntityViewModel<type1> and EntityViewModel<type2>.
 */
class OutGiScrapViewModel(application: Application): EntityViewModel<OutGiScrap>(application, EntitySets.outGISCRAPSet, OutGiScrap.lgort) {
    /**
     * Constructor for a specific view model with navigation data.
     * @param [navigationPropertyName] - name of the navigation property
     * @param [entityData] - parent entity (starting point of the navigation)
     */
    constructor(application: Application, navigationPropertyName: String, entityData: Parcelable): this(application) {
        EntityViewModel<OutGiScrap>(application, EntitySets.outGISCRAPSet, OutGiScrap.lgort, navigationPropertyName, entityData)
    }
}
