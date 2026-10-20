package com.example.appproject.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier to apply a bone structure shimmer loading gradient animation.
 */
@Composable
fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFFE2E8F0),
    highlightColor: Color = Color(0xFFF8FAFC)
): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerAnim"
    )

    val shimmerColors = listOf(
        baseColor,
        highlightColor,
        baseColor
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 400f, translateAnim - 400f),
        end = Offset(translateAnim, translateAnim)
    )

    return this.background(brush = brush, shape = shape)
}

/**
 * Generic Skeleton Box placeholder line or container.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    width: Dp? = null,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val boxModifier = if (width != null) {
        modifier.size(width = width, height = height)
    } else {
        modifier
            .fillMaxWidth()
            .height(height)
    }
    Box(modifier = boxModifier.shimmerEffect(shape = shape))
}

/**
 * Bone structure skeleton loader for the Help and Support Screen.
 */
@Composable
fun HelpScreenSkeleton(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        if (onBack != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                SkeletonBox(height = 36.dp, width = 36.dp, shape = CircleShape)
                Spacer(modifier = Modifier.width(12.dp))
                SkeletonBox(height = 20.dp, width = 120.dp, shape = RoundedCornerShape(6.dp))
            }
        }

        // Title & Description Skeletons
        SkeletonBox(height = 28.dp, width = 200.dp, shape = RoundedCornerShape(6.dp))
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonBox(height = 14.dp, width = 260.dp, shape = RoundedCornerShape(4.dp))

        Spacer(modifier = Modifier.height(20.dp))

        // 2x2 Grid Topic Category Skeletons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopicCategorySkeleton(modifier = Modifier.weight(1f))
            TopicCategorySkeleton(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopicCategorySkeleton(modifier = Modifier.weight(1f))
            TopicCategorySkeleton(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Common Questions Section Skeleton
        SkeletonBox(height = 22.dp, width = 180.dp, shape = RoundedCornerShape(6.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                repeat(4) { index ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SkeletonBox(height = 16.dp, width = (180 + (index * 20)).dp)
                            SkeletonBox(height = 18.dp, width = 18.dp, shape = CircleShape)
                        }
                    }
                    if (index < 3) {
                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Direct Support Section Skeleton
        SkeletonBox(height = 22.dp, width = 150.dp, shape = RoundedCornerShape(6.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                repeat(3) { index ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SkeletonBox(height = 36.dp, width = 36.dp, shape = CircleShape)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            SkeletonBox(height = 12.dp, width = 60.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            SkeletonBox(height = 14.dp, width = 140.dp)
                        }
                    }
                    if (index < 2) {
                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopicCategorySkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(height = 42.dp, width = 42.dp, shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.width(12.dp))
            SkeletonBox(height = 16.dp, width = 70.dp, shape = RoundedCornerShape(4.dp))
        }
    }
}

/**
 * Bone structure skeleton card for Booking items in MyBookingsScreen.
 */
@Composable
fun BookingCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Booking ID + Status Badge Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(height = 14.dp, width = 110.dp)
                SkeletonBox(height = 24.dp, width = 85.dp, shape = RoundedCornerShape(12.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body Row: Trip Image Thumbnail + Trip Title & Info Skeleton
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(height = 70.dp, width = 70.dp, shape = RoundedCornerShape(14.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonBox(height = 18.dp, width = 160.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonBox(height = 14.dp, width = 120.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonBox(height = 14.dp, width = 90.dp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer Row: Price Skeleton + Action Button Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    SkeletonBox(height = 12.dp, width = 60.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    SkeletonBox(height = 18.dp, width = 100.dp)
                }
                SkeletonBox(height = 36.dp, width = 110.dp, shape = RoundedCornerShape(18.dp))
            }
        }
    }
}

/**
 * Bone structure skeleton card for Voucher items in UserVouchersScreen.
 */
@Composable
fun VoucherCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Coupon Icon Box Skeleton
            SkeletonBox(height = 54.dp, width = 54.dp, shape = RoundedCornerShape(14.dp))
            Spacer(modifier = Modifier.width(16.dp))

            // Middle Text Skeleton
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkeletonBox(height = 18.dp, width = 100.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    SkeletonBox(height = 20.dp, width = 50.dp, shape = RoundedCornerShape(8.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBox(height = 14.dp, width = 180.dp)
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(height = 12.dp, width = 120.dp)
            }
        }
    }
}

/**
 * Bone structure skeleton card for Admin Help Requests.
 */
@Composable
fun HelpRequestCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(height = 16.dp, width = 130.dp)
                SkeletonBox(height = 22.dp, width = 75.dp, shape = RoundedCornerShape(10.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            SkeletonBox(height = 14.dp, width = 220.dp)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBox(height = 14.dp, width = 160.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                SkeletonBox(height = 12.dp, width = 80.dp)
            }
        }
    }
}

/**
 * Bone structure skeleton card for Admin List views (User Accounts, Vouchers, etc.).
 */
@Composable
fun AdminListItemSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(height = 40.dp, width = 40.dp, shape = CircleShape)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBox(height = 16.dp, width = 140.dp)
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(height = 12.dp, width = 180.dp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            SkeletonBox(height = 28.dp, width = 60.dp, shape = RoundedCornerShape(14.dp))
        }
    }
}

/**
 * Bone structure skeleton loader for the My Account Screen.
 */
@Composable
fun MyAccountScreenSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Screen Title Skeleton
        SkeletonBox(height = 32.dp, width = 180.dp, shape = RoundedCornerShape(8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Card Skeleton
        SkeletonBox(height = 100.dp, width = null, shape = RoundedCornerShape(20.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // Section 1 Header Skeleton
        SkeletonBox(height = 18.dp, width = 100.dp, shape = RoundedCornerShape(6.dp))
        Spacer(modifier = Modifier.height(10.dp))

        // Section 1 Card Skeleton (3 items)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                repeat(3) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBox(height = 40.dp, width = 40.dp, shape = RoundedCornerShape(12.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        SkeletonBox(height = 16.dp, width = 140.dp, shape = RoundedCornerShape(4.dp))
                    }
                    if (index < 2) {
                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 2 Header Skeleton
        SkeletonBox(height = 18.dp, width = 130.dp, shape = RoundedCornerShape(6.dp))
        Spacer(modifier = Modifier.height(10.dp))

        // Section 2 Card Skeleton (4 items)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                repeat(4) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBox(height = 40.dp, width = 40.dp, shape = RoundedCornerShape(12.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        SkeletonBox(height = 16.dp, width = 120.dp, shape = RoundedCornerShape(4.dp))
                    }
                    if (index < 3) {
                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout/Login Button Skeleton
        SkeletonBox(height = 52.dp, width = null, shape = RoundedCornerShape(16.dp))
    }
}
