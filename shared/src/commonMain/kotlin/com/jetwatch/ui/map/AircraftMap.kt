package com.jetwatch.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jetwatch.domain.model.Aircraft
import com.jetwatch.domain.model.BoundingBox

@Composable
expect fun AircraftMap(
    aircraft: List<Aircraft>,
    onBoundsChanged: (BoundingBox) -> Unit,
    onAircraftClick: (Aircraft) -> Unit,
    modifier: Modifier = Modifier,
)
