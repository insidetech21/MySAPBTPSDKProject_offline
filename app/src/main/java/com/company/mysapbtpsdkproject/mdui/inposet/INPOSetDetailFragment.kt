package com.company.mysapbtpsdkproject.mdui.inposet

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.company.mysapbtpsdkproject.databinding.FragmentInposetDetailBinding
import com.company.mysapbtpsdkproject.mdui.EntityKeyUtil
import com.company.mysapbtpsdkproject.mdui.InterfacedFragment
import com.company.mysapbtpsdkproject.mdui.UIConstants
import com.company.mysapbtpsdkproject.repository.OperationResult
import com.company.mysapbtpsdkproject.R
import com.company.mysapbtpsdkproject.viewmodel.inpo.InPoViewModel
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntitySets
import com.sap.cloud.android.odata.zcims_int_srv_entities.InPo
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader

import com.company.mysapbtpsdkproject.mdui.inpoitemset.INPOITEMSetActivity

/**
 * A fragment representing a single InPo detail screen.
 * This fragment is contained in an INPOSetActivity.
 */
class INPOSetDetailFragment : InterfacedFragment<InPo, FragmentInposetDetailBinding>() {

    /** InPo entity to be displayed */
    private lateinit var inPoEntity: InPo

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private var objectHeader: ObjectHeader? = null

    /** View model of the entity type that the displayed entity belongs to */
    private lateinit var viewModel: InPoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_view_options
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fragmentBinding.handler = this
        return fragmentBinding.root
    }

    override  fun  initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentInposetDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            currentActivity = it
            viewModel = ViewModelProvider(it)[InPoViewModel::class.java]
            viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
                onDeleteComplete(result)
            }

            viewModel.selectedEntity.observe(viewLifecycleOwner) { entity ->
                inPoEntity = entity
                fragmentBinding.inPo = entity
                setupObjectHeader()
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.update_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, inPoEntity)
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
    private fun onDeleteComplete(result: OperationResult<InPo>) {
        progressBar?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.removeAllSelected()
        result.error?.let {
            showError(getString(R.string.delete_failed_detail))
            return
        }
        listener?.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, inPoEntity)
    }


    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToINPOITEMSet_Navg_Po(view: View) {
        val intent = Intent(currentActivity, INPOITEMSetActivity::class.java)
        intent.putExtra("parent", inPoEntity)
        intent.putExtra("navigation", "Navg_Po")
        startActivity(intent)
    }

    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private fun setDetailImage(objectHeader: ObjectHeader, inPoEntity: InPo) {
        if (inPoEntity.getOptionalValue(InPo.bsart) != null && inPoEntity.getOptionalValue(InPo.bsart).toString().isNotEmpty()) {
            objectHeader.detailImageCharacter = inPoEntity.getOptionalValue(InPo.bsart).toString().substring(0, 1)
        } else {
            objectHeader.detailImageCharacter = "?"
        }
    }

    /**
     * Setup ObjectHeader with an instance of inPoEntity
     */
    private fun setupObjectHeader() {
        val secondToolbar = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)
        if (secondToolbar != null) {
            secondToolbar.title = inPoEntity.entityType.localName
        } else {
            currentActivity.title = inPoEntity.entityType.localName
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader)
        val dataValue = inPoEntity.getOptionalValue(InPo.bsart)

        objectHeader?.let {
            it.apply {
                headline = dataValue?.toString()
                subheadline = EntityKeyUtil.getOptionalEntityKey(inPoEntity)
                body = "You can set the header body text here."
                footnote = "You can set the header footnote here."
                description = "You can add a detailed item description here."
            }
            it.setTag("#tag1", 0)
            it.setTag("#tag3", 2)
            it.setTag("#tag2", 1)

            setDetailImage(it, inPoEntity)
            it.visibility = View.VISIBLE
        }
    }
}
