package com.way.samurai.koinstudy

import com.way.samurai.koinstudy.data.dispatchers.DispatcherProvider
import com.way.samurai.koinstudy.di.featureTasksModule
import com.way.samurai.koinstudy.domain.model.User
import com.way.samurai.koinstudy.domain.model.UserBio
import com.way.samurai.koinstudy.domain.model.UserTitle
import com.way.samurai.koinstudy.domain.repository.UserRepository
import com.way.samurai.koinstudy.domain.repository.UserState
import com.way.samurai.koinstudy.presentation.state.UserUiState
import com.way.samurai.koinstudy.presentation.state.UserViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit4.KoinTestRule
import org.koin.test.inject
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest : KoinTest {

    private val dispatcher = StandardTestDispatcher()
    private val repositoryState = MutableStateFlow<UserState>(UserState.Idle)

    private val testModule: Module = module(override = true) {
        single<DispatcherProvider> {
            object : DispatcherProvider {
                override val io: CoroutineDispatcher = dispatcher
                override val main: CoroutineDispatcher = dispatcher
            }
        }
        single<UserRepository> { FakeUserRepository(repositoryState) }
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(featureTasksModule + testModule)
    }

    private val viewModel: UserViewModel by inject()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `updates ui state with repository events`() = runTest(dispatcher) {
        assertEquals(UserUiState.Idle, viewModel.uiState.value)

        repositoryState.update { UserState.Loading }
        assertEquals(UserUiState.Loading, viewModel.uiState.value)

        val user = User("1", "Test", UserTitle("Engineer"), UserBio("Builds tests"))
        repositoryState.update { UserState.Data(user) }
        assertEquals(UserUiState.Content(user), viewModel.uiState.value)

        repositoryState.update { UserState.Error("boom") }
        assertEquals(UserUiState.Error("boom"), viewModel.uiState.value)
    }
}

private class FakeUserRepository(
    private val backingState: MutableStateFlow<UserState>,
) : UserRepository {
    override val userStream: StateFlow<UserState> = backingState.asStateFlow()

    override suspend fun refresh() {
        // no-op for tests
    }
}
