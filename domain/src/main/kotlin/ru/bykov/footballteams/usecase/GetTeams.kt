package ru.bykov.footballteams.usecase

import io.reactivex.Single
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import ru.bykov.footballteams.models.FootballTeam
import ru.bykov.footballteams.repository.FootballTeamRepository
import ru.bykov.footballteams.repository.StringPreferencesRepository

private const val LAST_UPDATE_DATE = "last_update_date"
class GetTeams(
    private val prefsRepository: StringPreferencesRepository,
    private val localRepository: FootballTeamRepository,
    private val remoteRepository: FootballTeamRepository
) : SimpleUseCase<Single<List<FootballTeam>>>{

    override fun invoke(): Single<List<FootballTeam>> {
        return prefsRepository.getPref(LAST_UPDATE_DATE, Instant.DISTANT_PAST.toString())
            .flatMap { lastUpdateDate ->
                val diff = Clock.System.now() - lastUpdateDate.toInstant()
                if (diff.isPositive() && diff.inWholeDays > 1) {
                    getRemoteTeams()
                } else {
                    localRepository.teams()
                }
            }
    }

    private fun getRemoteTeams(): Single<List<FootballTeam>> {
        return remoteRepository.teams().flatMap { teams ->
            prefsRepository.setPref(LAST_UPDATE_DATE, Clock.System.now().toString())
                .andThen(Single.just(teams))
        }
    }
}
