package com.cscareerguide.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteRegistryTest {
    @Test
    fun registryContainsExactlyTheEighteenWebRoutes() {
        assertEquals(18, RouteRegistry.allRoutes.size)
        assertEquals(18, RouteRegistry.allRoutes.map { it.navRoute }.toSet().size)
        assertEquals(18, RouteRegistry.allRoutes.map { it.webPath }.toSet().size)
    }

    @Test
    fun onlyAuthenticationRoutesArePublic() {
        assertEquals(
            setOf(Routes.LOGIN, Routes.REGISTER),
            RouteRegistry.publicRoutes,
        )
        assertEquals(16, RouteRegistry.protectedRoutes.size)
    }

    @Test
    fun authGuardStartsAtTheExpectedDestination() {
        assertEquals(Routes.LOGIN, RouteRegistry.initialDestination(false))
        assertEquals(Routes.HOME, RouteRegistry.initialDestination(true))
    }

    @Test
    fun authGuardRejectsProtectedTargetsForLoggedOutUsers() {
        assertEquals(
            Routes.LOGIN,
            RouteRegistry.safeDestination(Routes.RESUME, isAuthenticated = false),
        )
        assertEquals(
            Routes.RESUME,
            RouteRegistry.safeDestination(Routes.RESUME, isAuthenticated = true),
        )
        assertEquals(
            Routes.REGISTER,
            RouteRegistry.safeDestination(Routes.REGISTER, isAuthenticated = false),
        )
    }

    @Test
    fun fiveMainTabsAreRegisteredTopLevelRoutes() {
        assertEquals(5, MainTab.entries.size)
        val registeredRoutes = RouteRegistry.allRoutes.map { it.navRoute }.toSet()
        assertTrue(MainTab.entries.all { it.route in registeredRoutes })
        assertTrue(MainTab.entries.map { it.route }.toSet().size == 5)
    }
}
