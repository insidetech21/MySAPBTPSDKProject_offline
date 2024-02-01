package com.company.mysapbtpsdkproject.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.os.Parcelable

import com.company.mysapbtpsdkproject.viewmodel.inpoheader.InPoHeaderViewModel
import com.company.mysapbtpsdkproject.viewmodel.inpoitem.InPoItemViewModel
import com.company.mysapbtpsdkproject.viewmodel.inpo.InPoViewModel
import com.company.mysapbtpsdkproject.viewmodel.inpostloc.INPOStLocViewModel
import com.company.mysapbtpsdkproject.viewmodel.inprintmedia.InPrintMediaViewModel
import com.company.mysapbtpsdkproject.viewmodel.inprintpoitem.InPrintPoItemViewModel
import com.company.mysapbtpsdkproject.viewmodel.inprintpo.InPrintPoViewModel
import com.company.mysapbtpsdkproject.viewmodel.inreprintpo.InReprintPoViewModel
import com.company.mysapbtpsdkproject.viewmodel.inreprintrsn.InReprintRsnViewModel
import com.company.mysapbtpsdkproject.viewmodel.instockovw.InStockOvwViewModel
import com.company.mysapbtpsdkproject.viewmodel.intmattomatse.INTMattomatSeViewModel
import com.company.mysapbtpsdkproject.viewmodel.intmattomat.INTMattomatViewModel
import com.company.mysapbtpsdkproject.viewmodel.intmtomchargvalid.INTMtomChargvalidViewModel
import com.company.mysapbtpsdkproject.viewmodel.intphyinvpost.IntPhyInvPostViewModel
import com.company.mysapbtpsdkproject.viewmodel.intphyinv.IntPhyInvViewModel
import com.company.mysapbtpsdkproject.viewmodel.intphyvalid.IntPhyValidViewModel
import com.company.mysapbtpsdkproject.viewmodel.intsloctosloc.IntSlocToSlocViewModel
import com.company.mysapbtpsdkproject.viewmodel.intslocvalid.IntSlocValidViewModel
import com.company.mysapbtpsdkproject.viewmodel.loginsrv.LoginSrvViewModel
import com.company.mysapbtpsdkproject.viewmodel.matdetails.MatDetailsViewModel
import com.company.mysapbtpsdkproject.viewmodel.outgiscrap.OutGiScrapViewModel
import com.company.mysapbtpsdkproject.viewmodel.outgiso.OutGiSoViewModel
import com.company.mysapbtpsdkproject.viewmodel.outpicking1.OutPicking1ViewModel
import com.company.mysapbtpsdkproject.viewmodel.outpicking.OutPickingViewModel
import com.company.mysapbtpsdkproject.viewmodel.outgireservation.OutGIReservationViewModel
import com.company.mysapbtpsdkproject.viewmodel.rfmattag.RfMattagViewModel

/**
 * Custom factory class, which can create view models for entity subsets, which are
 * reached from a parent entity through a navigation property.
 *
 * @param application parent application
 * @param navigationPropertyName name of the navigation link
 * @param entityData parent entity
 */
class EntityViewModelFactory (
        val application: Application, // name of the navigation property
        val navigationPropertyName: String, // parent entity
        val entityData: Parcelable) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass.simpleName) {
			"InPoHeaderViewModel" -> InPoHeaderViewModel(application, navigationPropertyName, entityData) as T
            			"InPoItemViewModel" -> InPoItemViewModel(application, navigationPropertyName, entityData) as T
            			"InPoViewModel" -> InPoViewModel(application, navigationPropertyName, entityData) as T
            			"INPOStLocViewModel" -> INPOStLocViewModel(application, navigationPropertyName, entityData) as T
            			"InPrintMediaViewModel" -> InPrintMediaViewModel(application, navigationPropertyName, entityData) as T
            			"InPrintPoItemViewModel" -> InPrintPoItemViewModel(application, navigationPropertyName, entityData) as T
            			"InPrintPoViewModel" -> InPrintPoViewModel(application, navigationPropertyName, entityData) as T
            			"InReprintPoViewModel" -> InReprintPoViewModel(application, navigationPropertyName, entityData) as T
            			"InReprintRsnViewModel" -> InReprintRsnViewModel(application, navigationPropertyName, entityData) as T
            			"InStockOvwViewModel" -> InStockOvwViewModel(application, navigationPropertyName, entityData) as T
            			"INTMattomatSeViewModel" -> INTMattomatSeViewModel(application, navigationPropertyName, entityData) as T
            			"INTMattomatViewModel" -> INTMattomatViewModel(application, navigationPropertyName, entityData) as T
            			"INTMtomChargvalidViewModel" -> INTMtomChargvalidViewModel(application, navigationPropertyName, entityData) as T
            			"IntPhyInvPostViewModel" -> IntPhyInvPostViewModel(application, navigationPropertyName, entityData) as T
            			"IntPhyInvViewModel" -> IntPhyInvViewModel(application, navigationPropertyName, entityData) as T
            			"IntPhyValidViewModel" -> IntPhyValidViewModel(application, navigationPropertyName, entityData) as T
            			"IntSlocToSlocViewModel" -> IntSlocToSlocViewModel(application, navigationPropertyName, entityData) as T
            			"IntSlocValidViewModel" -> IntSlocValidViewModel(application, navigationPropertyName, entityData) as T
            			"LoginSrvViewModel" -> LoginSrvViewModel(application, navigationPropertyName, entityData) as T
            			"MatDetailsViewModel" -> MatDetailsViewModel(application, navigationPropertyName, entityData) as T
            			"OutGiScrapViewModel" -> OutGiScrapViewModel(application, navigationPropertyName, entityData) as T
            			"OutGiSoViewModel" -> OutGiSoViewModel(application, navigationPropertyName, entityData) as T
            			"OutPicking1ViewModel" -> OutPicking1ViewModel(application, navigationPropertyName, entityData) as T
            			"OutPickingViewModel" -> OutPickingViewModel(application, navigationPropertyName, entityData) as T
            			"OutGIReservationViewModel" -> OutGIReservationViewModel(application, navigationPropertyName, entityData) as T
             else -> RfMattagViewModel(application, navigationPropertyName, entityData) as T
        }
    }
}
