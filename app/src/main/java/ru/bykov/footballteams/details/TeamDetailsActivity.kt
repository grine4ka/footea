package ru.bykov.footballteams.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.bykov.footballteams.R
import ru.bykov.footballteams.databinding.ActivityTeamDetailsBinding
import ru.bykov.footballteams.di.TeamDetailsInjection
import ru.bykov.footballteams.extensions.toast
import ru.bykov.footballteams.models.FootballTeamDetails

private const val EXTRA_TEAM_ID = "extra_team_id"
private const val NO_TEAM_ID = -1

fun Activity.showTeamDetails(teamId: Int) {
    startActivity(
        Intent(this, TeamDetailsActivity::class.java).apply {
            putExtra(EXTRA_TEAM_ID, teamId)
        }
    )
}

class TeamDetailsActivity : AppCompatActivity(), TeamDetailsContract.View {

    private val injection: TeamDetailsInjection by lazy(LazyThreadSafetyMode.NONE) {
        TeamDetailsInjection(this)
    }

    private val viewBinding: ActivityTeamDetailsBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityTeamDetailsBinding.inflate(layoutInflater)
    }

    private lateinit var presenter: TeamDetailsContract.Presenter

    // region Activity Callbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        presenter = injection.presenter
        presenter.loadTeamDetails(intent.getIntExtra(EXTRA_TEAM_ID, NO_TEAM_ID))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }
    // endregion

    // region TeamDetailsContract.View
    override fun showDetails(details: FootballTeamDetails) {
        supportActionBar?.title = details.name
        viewBinding.teamNameLabel.text = details.name
        viewBinding.gender.text = details.gender.toString()
        viewBinding.national.text = details.national.toString()
        viewBinding.description.text = details.description
    }

    override fun showError() {
        toast(getString(R.string.default_error))
    }
    // endregion

}