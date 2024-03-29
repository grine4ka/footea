package com.bykov.footea.details

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bykov.footea.FooteaApplication
import com.bykov.footea.R
import com.bykov.footea.di.AppContainer
import com.bykov.footea.di.TeamDetailsContainer
import com.bykov.footea.extensions.getColorFromAttr
import com.bykov.footea.extensions.toast
import com.bykov.footea.models.FootballTeamDetails
import com.bykov.footea.teamlineupview.HalfFieldDrawable
import com.bykov.footea.teamlineupview.PlayerAvatarNameView
import com.bykov.footea.teamlineupview.model.Player
import com.bykov.footea.ui.PaletteRequestListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.R as materialR


private const val EXTRA_TEAM_ID = "extra_team_id"
private const val EXTRA_TEAM_NAME = "extra_team_name"

private const val NO_TEAM_ID = -1

fun Activity.showTeamDetails(teamId: Int, teamName: String) {
    startActivity(
        Intent(this, TeamDetailsActivity::class.java).apply {
            putExtra(EXTRA_TEAM_ID, teamId)
            putExtra(EXTRA_TEAM_NAME, teamName)
        }
    )
}

class TeamDetailsActivity : AppCompatActivity(R.layout.activity_team_details), TeamDetailsContract.View {

    private lateinit var presenter: TeamDetailsContract.Presenter
    private lateinit var appContainer: AppContainer

    private val appBarLayout: AppBarLayout by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.app_bar_layout)
    }

    private val collapsingToolbarLayout: CollapsingToolbarLayout by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.collapsing_toolbar)
    }

    private val toolbar: Toolbar by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.toolbar)
    }

    private val toolbarTitle: TextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.toolbar_title)
    }

    private val teamBadge: ImageView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.team_badge)
    }

    private val teamName: TextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.team_name)
    }

    private val national: TextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.national)
    }

    private val venue: TextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.venue)
    }

    private val lineupContent: View by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.lineup_card_content)
    }

    private val appBarOffsetChangeListener = AppBarOffsetChangeListener(
        onOffsetChanged = { _, offsetRange ->
            teamBadge.alpha = 1 - offsetRange
            national.alpha = 1 - offsetRange
            titleViewAnimator.moveTitleView(offsetRange)
        }
    )

    private val titleViewAnimator: TitleViewAnimator = TitleViewAnimator()

    // region Activity Callbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = (application as FooteaApplication).appContainer
        appContainer.teamDetailsContainer = TeamDetailsContainer(
            appContainer.localRepository
        )
        presenter = appContainer.teamDetailsContainer!!.presenter(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        appBarLayout.addOnOffsetChangedListener(appBarOffsetChangeListener)
        titleViewAnimator.onViewsCreated(teamName, toolbarTitle)
        // TODO refactor this in #46
        lineupContent.background = HalfFieldDrawable(this)
        lineupContent.findViewById<PlayerAvatarNameView>(R.id.player_1).bind(
            Player(
                name = "Lisandro Martínez",
                avatarUrl = "https://media-1.api-sports.io/football/players/2467.png",
                number = 6,
                position = "Defender",
            )
        )

        presenter.loadTeamDetails(intent.getIntExtra(EXTRA_TEAM_ID, NO_TEAM_ID))
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            titleViewAnimator.onWindowFocused(appBarOffsetChangeListener.currentOffset)
        }
    }
    override fun onDestroy() {
        presenter.destroy()
        titleViewAnimator.onViewsDestroyed()
        appBarLayout.removeOnOffsetChangedListener(appBarOffsetChangeListener)
        appContainer.teamDetailsContainer = null
        super.onDestroy()
    }
    // endregion

    // region TeamDetailsContract.View
    override fun showDetails(details: FootballTeamDetails) {
        Glide.with(this)
            .asBitmap()
            .listener(PaletteRequestListener(::animateColorChange))
            .load(details.badgeUrl)
            .error(R.drawable.ic_launcher_background)
            .into(teamBadge)
        teamName.text = details.name
        national.text = details.country
        venue.text = details.venue
        titleViewAnimator.onTitleTextChanged(appBarOffsetChangeListener.currentOffset)
    }

    override fun showError(message: String?) {
        toast(message ?: getString(R.string.default_error))
    }
    // endregion

    private fun animateColorChange(
        domainColor: Int,
        lightColor: Int,
        textColor: Int
    ) {
        animateDomainColor(domainColor)
        animateLightColor(lightColor)
        animateTextColor(textColor)
    }

    private fun animateDomainColor(domainColor: Int) {
        val colorFrom = getColorFromAttr(materialR.attr.colorPrimary)
        animateColor(colorFrom, domainColor) { color ->
            appBarLayout.setBackgroundColor(color)
            toolbar.setBackgroundColor(color)
            collapsingToolbarLayout.contentScrim = ColorDrawable(color)
        }
    }

    private fun animateLightColor(lightColor: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        val colorFrom = getColorFromAttr(materialR.attr.colorPrimaryVariant)
        animateColor(colorFrom, lightColor) { color ->
            collapsingToolbarLayout.statusBarScrim = ColorDrawable(color)
            window.navigationBarColor = color
            window.statusBarColor = color
        }
    }

    private fun animateTextColor(textColor: Int) {
        val colorFrom = teamName.currentTextColor
        animateColor(colorFrom, textColor) { color ->
            teamName.setTextColor(color)
            national.setTextColor(color)
        }
    }

    private fun animateColor(@ColorInt colorFrom: Int, colorTo: Int, onColorChange: (Int) -> Unit) {
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
            duration = 250 // milliseconds
            addUpdateListener { animator ->
                onColorChange(animator.animatedValue as Int)
            }
        }
        colorAnimation.start()
    }
}
