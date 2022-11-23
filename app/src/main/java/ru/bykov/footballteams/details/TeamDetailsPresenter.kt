package ru.bykov.footballteams.details

import io.reactivex.disposables.CompositeDisposable
import ru.bykov.footballteams.extensions.async
import ru.bykov.footballteams.repository.FootballTeamRepository

class TeamDetailsPresenter(
    private val repository: FootballTeamRepository,
    private val view: TeamDetailsContract.View,
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
) : TeamDetailsContract.Presenter {

    override fun loadTeamDetails(teamId: Int) {
        compositeDisposable.add(
            repository.details(teamId)
                .async()
                .subscribe(
                    { view.showDetails(it) },
                    { view.showError(it.message) }
                )
        )
    }

    override fun destroy() {
        compositeDisposable.clear()
    }
}