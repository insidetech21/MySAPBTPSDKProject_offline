package com.company.mysapbtpsdkproject.mdui.intphyinvpostset

import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import com.company.mysapbtpsdkproject.R
import com.company.mysapbtpsdkproject.databinding.FragmentIntphyinvpostsetCreateBinding
import com.company.mysapbtpsdkproject.mdui.BundleKeys
import com.company.mysapbtpsdkproject.mdui.InterfacedFragment
import com.company.mysapbtpsdkproject.mdui.UIConstants
import com.company.mysapbtpsdkproject.repository.OperationResult
import com.company.mysapbtpsdkproject.viewmodel.intphyinvpost.IntPhyInvPostViewModel
import com.sap.cloud.android.odata.zcims_int_srv_entities.IntPhyInvPost
import com.sap.cloud.android.odata.zcims_int_srv_entities.ZCIMS_INT_SRV_EntitiesMetadata.EntityTypes
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader
import com.sap.cloud.mobile.odata.Property
import org.slf4j.LoggerFactory

/**
 * A fragment that is used for both update and create for users to enter values for the properties. When used for
 * update, an instance of the entity is required. In the case of create, a new instance of the entity with defaults will
 * be created. The default values may not be acceptable for the OData service.
 * This fragment is either contained in a [IntPhyInvPostSetListActivity] in two-pane mode (on tablets) or a
 * [IntPhyInvPostSetDetailActivity] on handsets.
 *
 * Arguments: Operation: [OP_CREATE | OP_UPDATE]
 *            IntPhyInvPost if Operation is update
 */
class IntPhyInvPostSetCreateFragment : InterfacedFragment<IntPhyInvPost, FragmentIntphyinvpostsetCreateBinding>() {

    /** IntPhyInvPost object and it's copy: the modifications are done on the copied object. */
    private lateinit var intPhyInvPostEntity: IntPhyInvPost
    private lateinit var intPhyInvPostEntityCopy: IntPhyInvPost

    /** Indicate what operation to be performed */
    private lateinit var operation: String

    /** intPhyInvPostEntity ViewModel */
    private lateinit var viewModel: IntPhyInvPostViewModel

    /** The update menu item */
    private lateinit var updateMenuItem: MenuItem

    private val isIntPhyInvPostValid: Boolean
        get() {
            var isValid = true
            fragmentBinding.createUpdateIntphyinvpost.let { linearLayout ->
                for (i in 0 until linearLayout.childCount) {
                    val simplePropertyFormCell = linearLayout.getChildAt(i) as SimplePropertyFormCell
                    val propertyName = simplePropertyFormCell.tag as String
                    val property = EntityTypes.intPhyInvPost.getProperty(propertyName)
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
                    UIConstants.OP_CREATE -> resources.getString(R.string.title_create_fragment, EntityTypes.intPhyInvPost.localName)
                    else -> resources.getString(R.string.title_update_fragment) + " " + EntityTypes.intPhyInvPost.localName

                }
            }
        }

        activity?.let {
            (it as IntPhyInvPostSetActivity).isNavigationDisabled = true
            viewModel = ViewModelProvider(it)[IntPhyInvPostViewModel::class.java]
            viewModel.createResult.observe(this) { result -> onComplete(result) }
            viewModel.updateResult.observe(this) { result -> onComplete(result) }

            intPhyInvPostEntity = if (operation == UIConstants.OP_CREATE) {
                createIntPhyInvPost()
            } else {
                viewModel.selectedEntity.value!!
            }

            val workingCopy = when{ (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) -> {
                    savedInstanceState?.getParcelable<IntPhyInvPost>(KEY_WORKING_COPY, IntPhyInvPost::class.java)
                } else -> @Suppress("DEPRECATION") savedInstanceState?.getParcelable<IntPhyInvPost>(KEY_WORKING_COPY)
            }

            if (workingCopy == null) {
                intPhyInvPostEntityCopy = intPhyInvPostEntity.copy()
                intPhyInvPostEntityCopy.entityTag = intPhyInvPostEntity.entityTag
                intPhyInvPostEntityCopy.oldEntity = intPhyInvPostEntity
                intPhyInvPostEntityCopy.editLink = intPhyInvPostEntity.editLink
            } else {
                intPhyInvPostEntityCopy = workingCopy
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        currentActivity.findViewById<ObjectHeader>(R.id.objectHeader)?.let {
            it.visibility = View.GONE
        }
        fragmentBinding.intPhyInvPost = intPhyInvPostEntityCopy
        return fragmentBinding.root
    }

    override  fun  initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentIntphyinvpostsetCreateBinding.inflate(inflater, container, false)

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
        outState.putParcelable(KEY_WORKING_COPY, intPhyInvPostEntityCopy)
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
        if (!isIntPhyInvPostValid) {
            return false
        }
        (currentActivity as IntPhyInvPostSetActivity).isNavigationDisabled = false
        progressBar?.visibility = View.VISIBLE
        when (operation) {
            UIConstants.OP_CREATE -> {
                viewModel.create(intPhyInvPostEntityCopy)
            }
            UIConstants.OP_UPDATE -> viewModel.update(intPhyInvPostEntityCopy)
        }
        return true
    }

    /**
     * Create a new IntPhyInvPost instance and initialize properties to its default values
     * Nullable property will remain null
     * For offline, keys will be unset to avoid collision should more than one is created locally
     * @return new IntPhyInvPost instance
     */
    private fun createIntPhyInvPost(): IntPhyInvPost {
        val entity = IntPhyInvPost(true)
        entity.unsetDataValue(IntPhyInvPost.matnr)
        return entity
    }

    /** Callback function to complete processing when updateResult or createResult events fired */
    private fun onComplete(result: OperationResult<IntPhyInvPost>) {
        progressBar?.visibility = View.INVISIBLE
        enableUpdateMenuItem(true)
        if (result.error != null) {
            (currentActivity as IntPhyInvPostSetActivity).isNavigationDisabled = true
            handleError(result)
        } else {
            if (operation == UIConstants.OP_UPDATE && !currentActivity.resources.getBoolean(R.bool.two_pane)) {
                viewModel.selectedEntity.value = intPhyInvPostEntityCopy
            }
            if (currentActivity.resources.getBoolean(R.bool.two_pane)) {
                val listFragment = currentActivity.supportFragmentManager.findFragmentByTag(UIConstants.LIST_FRAGMENT_TAG)
                (listFragment as IntPhyInvPostSetListFragment).refreshListData()
            }
            (currentActivity as IntPhyInvPostSetActivity).onBackPressedDispatcher.onBackPressed()
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
    private fun handleError(result: OperationResult<IntPhyInvPost>) {
        val errorMessage = when (result.operation) {
            OperationResult.Operation.UPDATE -> getString(R.string.update_failed_detail)
            OperationResult.Operation.CREATE -> getString(R.string.create_failed_detail)
            else -> throw AssertionError()
        }
        showError(errorMessage)
    }


    companion object {
        private val KEY_WORKING_COPY = "WORKING_COPY"
        private val LOGGER = LoggerFactory.getLogger(IntPhyInvPostSetActivity::class.java)
    }
}
