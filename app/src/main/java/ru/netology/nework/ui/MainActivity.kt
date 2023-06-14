package ru.netology.nework.ui

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.adapter.loadCircleCrop
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.ActivityMainBinding
import ru.netology.nework.databinding.NavHeaderMainBinding
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHeaderBinding: NavHeaderMainBinding

    @Inject
    lateinit var appAuth: AppAuth

    private val userViewModel: UserViewModel by viewModels()
    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHeaderBinding = NavHeaderMainBinding.bind(binding.navView.getHeaderView(0))

        setSupportActionBar(binding.appBarMain.toolbar)

        val previousMenuProvider: MenuProvider? = null

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = this.findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_feed,
                R.id.nav_events,
                R.id.nav_my_wall,
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        var showGroup = true

        authViewModel.data.observe(this) {
            setupUser()
        }

        navHeaderBinding.loginMenu.setOnClickListener {
            authViewModel.data.observe(this) {
                showGroup = if (showGroup) {
                    previousMenuProvider?.let(::removeMenuProvider)
                    navView.menu.setGroupVisible(R.id.unauthorized, !authViewModel.authorized)
                    navView.menu.setGroupVisible(R.id.authorized, authViewModel.authorized)
                    false
                } else {
                    previousMenuProvider?.let(::removeMenuProvider)
                    navView.menu.setGroupVisible(R.id.unauthorized, false)
                    navView.menu.setGroupVisible(R.id.authorized, false)
                    true
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupUser() {
        userViewModel.getUserById()

        userViewModel.user.observe(this) { user ->
            with(navHeaderBinding) {

                if (authViewModel.authorized) {
                    if (user.avatar != null) {
                        avatarNavHeader.isVisible = true
                        val urlAvatars = "${user.avatar}"
                        avatarNavHeader.loadCircleCrop(urlAvatars)
                    }

                    nameNavHeader.text = user.name
                    loginNavHeader.text = user.login
                } else {
                    avatarNavHeader.isVisible = false
                    nameNavHeader.text = null
                    loginNavHeader.text = null
                }
            }
        }
    }
}