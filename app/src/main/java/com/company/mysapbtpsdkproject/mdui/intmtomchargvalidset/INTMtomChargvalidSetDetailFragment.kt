package com.company.mysapbtpsdkproject.mdui.intmtomchargvalidset

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.company.mysapbtpsdkproject.databinding.FragmentIntmtomchargvalidsetDetailBinding
import com.company.mysapbtpsdkproject.mdui.EntityKeyUtil
import com.company.mysapbtpsdkproject.mdui.InterfacedFragment
import com.company.mysapbtpsdkproject.mdui.UIConstants
import com.company.mysapbtpsdkproject.repository.OperationResult
import com.company.mysapbtpsdkproject.R
import com.company.mysapbtpsdkproject.viewmodel.intmtomchargvalid.INTMtomChargvalidViewModel
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntitySets
import com.sap.cloud.android.odata.zcims_int_srv_entities.INTMtomChargvalid
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader


/**
 * A fragment representing a single INTMtomChargvalid detail screen.
 * This fragment is contained in an INTMtomChargvalidSetActivity.
 */
class INTMtomChargvalidSetDetailFragment : InterfacedFragment<INTMtomChargvalid, FragmentIntmtomchargvalidsetDetailBinding>() {

    /** INTMtomChargvalid entity to be displayed */
    private lateinit var intMtomChargvalidEntity: INTMtomChargvalid

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private var objectHeader: ObjectHeader? = null

    /** View model of the entity type that the displayed entity belongs to */
    private lateinit var viewModel: INTMtomChargvalidViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_view_options
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fragmentBinding.handler = this
        return fragmentBinding.root
    }

    override  fun  initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentIntmtomchargvalidsetDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            currentActivity = it
            viewModel = ViewModelProvider(it)[INTMtomChargvalidViewModel::class.java]
            viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
                onDeleteComplete(result)
            }

            viewModel.selectedEntity.observe(viewLifecycleOwner) { entity ->
                intMtomChargvalidEntity = entity
                fragmentBinding.intMtomChargvalid = entity
                setupObjectHeader()
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.update_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, intMtomChargvalidEntity)
                true
            }
            R.id.delete_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_ASK_DELETE_CONFIRMATION,null)
                true
            }
            else -> super.onMenuItemSelected(menuItem)
        }
    }

    /**
     * Completion callback for delete operation
     *
     * @param [result] of the operation
     */
    private fun onDeleteComplete(result: OperationResult<INTMtomChargvalid>) {
        progressBar?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.removeAllSelected()
        result.error?.let {
            showError(getString(R.string.delete_failed_detail))
            return
        }
        listener?.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, intMtomChargvalidEntity)
    }


    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private fun setDetailImage(objectHeader: ObjectHeader, intMtomChargvalidEntity: INTMtomChargvalid) {
        if (intMtomChargvalidEntity.getOptionalValue(INTMtomChargvalid.maktx) != null && intMtomChargvalidEntity.getOptionalValue(INTMtomChargvalid.maktx).toString().isNotEmpty()) {
            objectHeader.detailImageCharacter = intMtomChargvalidEntity.getOptionalValue(INTMtomChargvalid.maktx).toString().substring(0, 1)
        } else {
            objectHeader.detailImageCharacter = "?"
        }
    }

    /**
     * Setup ObjectHeader with an instance of intMtomChargvalidEntity
     */
    private fun setupObjectHeader() {
        val secondToolbar = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)
        if (secondToolbar != null) {
            secondToolbar.title = intMtomChargvalidEntity.entityType.localName
        } else {
            currentActivity.title = intMtomChargvalidEntity.entityType.localName
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader)
        val dataValue = intMtomChargvalidEntity.getOptionalValue(INTMtomChargvalid.maktx)

        objectHeader?.let {
            it.apply {
                headline = dataValue?.toString()
                subheadline = EntityKeyUtil.getOptionalEntityKey(intMtomChargvalidEntity)
                body = "You can set the header body text here."
                footnote = "You can set the header footnote here."
                description = "You can add a detailed item description here."
            }
            it.setTag("#tag1", 0)
            it.setTag("#tag3", 2)
            it.setTag("#tag2", 1)

            setDetailImage(it, intMtomChargvalidEntity)
            it.visibility = View.VISIBLE
        }
    }
}
