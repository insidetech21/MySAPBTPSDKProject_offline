package com.company.mysapbtpsdkproject.mdui.inprintmediaset

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.company.mysapbtpsdkproject.databinding.FragmentInprintmediasetDetailBinding
import com.company.mysapbtpsdkproject.mdui.EntityKeyUtil
import com.company.mysapbtpsdkproject.mdui.InterfacedFragment
import com.company.mysapbtpsdkproject.mdui.UIConstants
import com.company.mysapbtpsdkproject.repository.OperationResult
import com.company.mysapbtpsdkproject.R
import com.company.mysapbtpsdkproject.viewmodel.inprintmedia.InPrintMediaViewModel
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntitySets
import com.sap.cloud.android.odata.zcims_int_srv_entities.InPrintMedia
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader

import android.widget.ImageView
import com.sap.cloud.mobile.odata.core.Action1
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable

/**
 * A fragment representing a single InPrintMedia detail screen.
 * This fragment is contained in an INPRINTMEDIASetActivity.
 */
class INPRINTMEDIASetDetailFragment : InterfacedFragment<InPrintMedia, FragmentInprintmediasetDetailBinding>() {

    /** InPrintMedia entity to be displayed */
    private lateinit var inPrintMediaEntity: InPrintMedia

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private var objectHeader: ObjectHeader? = null

    /** View model of the entity type that the displayed entity belongs to */
    private lateinit var viewModel: InPrintMediaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_view_options
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fragmentBinding.handler = this
        return fragmentBinding.root
    }

    override  fun  initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentInprintmediasetDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            currentActivity = it
            viewModel = ViewModelProvider(it)[InPrintMediaViewModel::class.java]
            viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
                onDeleteComplete(result)
            }

            viewModel.selectedEntity.observe(viewLifecycleOwner) { entity ->
                inPrintMediaEntity = entity
                fragmentBinding.inPrintMedia = entity
                setupObjectHeader()
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.update_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, inPrintMediaEntity)
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
    private fun onDeleteComplete(result: OperationResult<InPrintMedia>) {
        progressBar?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.removeAllSelected()
        result.error?.let {
            showError(getString(R.string.delete_failed_detail))
            return
        }
        listener?.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, inPrintMediaEntity)
    }


    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private fun setDetailImage(objectHeader: ObjectHeader, inPrintMediaEntity: InPrintMedia) {
        viewModel.downloadMedia(inPrintMediaEntity, Action1 { media ->
            objectHeader.prepareDetailImageView().scaleType = ImageView.ScaleType.FIT_CENTER
            val image = BitmapDrawable(currentActivity.resources, BitmapFactory.decodeByteArray(media, 0, media.size))
            objectHeader.detailImage = image
        }, Action1 {
            if (inPrintMediaEntity.getOptionalValue(InPrintMedia.menge) != null && inPrintMediaEntity.getOptionalValue(InPrintMedia.menge).toString().isNotEmpty()) {
                objectHeader.detailImageCharacter = inPrintMediaEntity.getOptionalValue(InPrintMedia.menge).toString().substring(0, 1)
            } else {
                objectHeader.detailImageCharacter = "?"
            }
        })
    }

    /**
     * Setup ObjectHeader with an instance of inPrintMediaEntity
     */
    private fun setupObjectHeader() {
        val secondToolbar = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)
        if (secondToolbar != null) {
            secondToolbar.title = inPrintMediaEntity.entityType.localName
        } else {
            currentActivity.title = inPrintMediaEntity.entityType.localName
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader)
        val dataValue = inPrintMediaEntity.getOptionalValue(InPrintMedia.menge)

        objectHeader?.let {
            it.apply {
                headline = dataValue?.toString()
                subheadline = EntityKeyUtil.getOptionalEntityKey(inPrintMediaEntity)
                body = "You can set the header body text here."
                footnote = "You can set the header footnote here."
                description = "You can add a detailed item description here."
            }
            it.setTag("#tag1", 0)
            it.setTag("#tag3", 2)
            it.setTag("#tag2", 1)

            setDetailImage(it, inPrintMediaEntity)
            it.visibility = View.VISIBLE
        }
    }
}
