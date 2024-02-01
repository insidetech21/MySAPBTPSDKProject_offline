package com.company.mysapbtpsdkproject.mdui.outgireservationset

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.company.mysapbtpsdkproject.databinding.FragmentOutgireservationsetDetailBinding
import com.company.mysapbtpsdkproject.mdui.EntityKeyUtil
import com.company.mysapbtpsdkproject.mdui.InterfacedFragment
import com.company.mysapbtpsdkproject.mdui.UIConstants
import com.company.mysapbtpsdkproject.repository.OperationResult
import com.company.mysapbtpsdkproject.R
import com.company.mysapbtpsdkproject.viewmodel.outgireservation.OutGIReservationViewModel
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntitySets
import com.sap.cloud.android.odata.zcims_int_srv_entities.OutGIReservation
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader

import com.company.mysapbtpsdkproject.mdui.outgireservationset.OutGIReservationSetActivity

/**
 * A fragment representing a single OutGIReservation detail screen.
 * This fragment is contained in an OutGIReservationSetActivity.
 */
class OutGIReservationSetDetailFragment : InterfacedFragment<OutGIReservation, FragmentOutgireservationsetDetailBinding>() {

    /** OutGIReservation entity to be displayed */
    private lateinit var outGIReservationEntity: OutGIReservation

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private var objectHeader: ObjectHeader? = null

    /** View model of the entity type that the displayed entity belongs to */
    private lateinit var viewModel: OutGIReservationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_view_options
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fragmentBinding.handler = this
        return fragmentBinding.root
    }

    override  fun  initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentOutgireservationsetDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            currentActivity = it
            viewModel = ViewModelProvider(it)[OutGIReservationViewModel::class.java]
            viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
                onDeleteComplete(result)
            }

            viewModel.selectedEntity.observe(viewLifecycleOwner) { entity ->
                outGIReservationEntity = entity
                fragmentBinding.outGIReservation = entity
                setupObjectHeader()
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.update_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, outGIReservationEntity)
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
    private fun onDeleteComplete(result: OperationResult<OutGIReservation>) {
        progressBar?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.removeAllSelected()
        result.error?.let {
            showError(getString(R.string.delete_failed_detail))
            return
        }
        listener?.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, outGIReservationEntity)
    }


    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToOutGIReservationSet_npo_resv(view: View) {
        val intent = Intent(currentActivity, OutGIReservationSetActivity::class.java)
        intent.putExtra("parent", outGIReservationEntity)
        intent.putExtra("navigation", "npo_resv")
        startActivity(intent)
    }

    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private fun setDetailImage(objectHeader: ObjectHeader, outGIReservationEntity: OutGIReservation) {
        if (outGIReservationEntity.getOptionalValue(OutGIReservation.maktx) != null && outGIReservationEntity.getOptionalValue(OutGIReservation.maktx).toString().isNotEmpty()) {
            objectHeader.detailImageCharacter = outGIReservationEntity.getOptionalValue(OutGIReservation.maktx).toString().substring(0, 1)
        } else {
            objectHeader.detailImageCharacter = "?"
        }
    }

    /**
     * Setup ObjectHeader with an instance of outGIReservationEntity
     */
    private fun setupObjectHeader() {
        val secondToolbar = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)
        if (secondToolbar != null) {
            secondToolbar.title = outGIReservationEntity.entityType.localName
        } else {
            currentActivity.title = outGIReservationEntity.entityType.localName
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader)
        val dataValue = outGIReservationEntity.getOptionalValue(OutGIReservation.maktx)

        objectHeader?.let {
            it.apply {
                headline = dataValue?.toString()
                subheadline = EntityKeyUtil.getOptionalEntityKey(outGIReservationEntity)
                body = "You can set the header body text here."
                footnote = "You can set the header footnote here."
                description = "You can add a detailed item description here."
            }
            it.setTag("#tag1", 0)
            it.setTag("#tag3", 2)
            it.setTag("#tag2", 1)

            setDetailImage(it, outGIReservationEntity)
            it.visibility = View.VISIBLE
        }
    }
}
