package com.example.appproject.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.appproject.R

/**
 * A dialog displaying a successful booking message with contact info and a return button,
 * styled matching the provided reference image.
 */
@Composable
fun BookingSuccessDialog(
    onDismiss: () -> Unit,
    onBackToHome: () -> Unit = onDismiss
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        BookingSuccessContent(
            onDismiss = onDismiss,
            onBackToHome = onBackToHome
        )
    }
}

@Composable
fun BookingSuccessContent(
    onDismiss: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .widthIn(max = 440.dp)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
        ) {
            // Soft Light-Blue Background Accents (Top-Left & Bottom-Right Curves)
            Canvas(modifier = Modifier.matchParentSize()) {
                // Top-Left Curve Accent
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE),
                            Color(0x80E0F2FE),
                            Color.Transparent
                        )
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(0f, 0f)
                )

                // Bottom-Right Curve Accent
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE),
                            Color(0x80E0F2FE),
                            Color.Transparent
                        )
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width, size.height)
                )
            }

            // Close Button ('X') in Top-Right
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Main Content Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glowing Checkmark Badge with Radiating Burst Dashes
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .padding(bottom = 8.dp)
                ) {
                    // Outermost Soft Green Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(90.dp)
                            .background(Color(0xFFE6F7F0), CircleShape)
                    ) {
                        // Middle Green Halo Ring
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(74.dp)
                                .background(Color(0xFFD1FAE5), CircleShape)
                        ) {
                            // Inner Solid Green Circle
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title: "Booking Successful!"
                Text(
                    text = stringResource(R.string.booking_successful),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F2B48),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle: "Our team will contact you within 24 hours for confirmation..."
                Text(
                    text = stringResource(R.string.booking_success_contact_desc),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Info Box Container (WhatsApp & Email Options)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF0F6FF))
                        .border(1.dp, Color(0xFFE2EAF4), RoundedCornerShape(16.dp))
                        .padding(vertical = 14.dp, horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // WhatsApp Info Item
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF25D366), CircleShape)
                            ) {
                                Icon(
                                    imageVector = WhatsAppIconVector,
                                    contentDescription = "WhatsApp",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.whatsapp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F2B48)
                                )
                            }
                        }

                        // Vertical Divider Line
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .background(Color(0xFFD0DCEB))
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Email Info Item
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF007AFF), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.email),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F2B48)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Button: "Back to Home ->"
                Button(
                    onClick = onBackToHome,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF)
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp, horizontal = 24.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.back_to_home),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ImageVector representation of WhatsApp icon.
 */
val WhatsAppIconVector: ImageVector by lazy {
    ImageVector.Builder(
        name = "WhatsApp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.White)
        ) {
            moveTo(12.04f, 2f)
            curveTo(6.58f, 2f, 2.13f, 6.45f, 2.13f, 11.91f)
            curveTo(2.13f, 14.01f, 2.78f, 15.96f, 3.89f, 17.58f)
            lineTo(2.5f, 21.5f)
            lineTo(6.57f, 20.17f)
            curveTo(8.13f, 21.07f, 9.93f, 21.58f, 12.04f, 21.58f)
            curveTo(17.5f, 21.58f, 21.95f, 17.13f, 21.95f, 11.91f)
            curveTo(21.95f, 6.45f, 17.5f, 2f, 12.04f, 2f)
            close()
            moveTo(12.04f, 18.48f)
            curveTo(10.24f, 18.48f, 8.56f, 17.96f, 7.14f, 17.06f)
            lineTo(6.79f, 16.84f)
            lineTo(4.37f, 17.63f)
            lineTo(5.17f, 15.27f)
            lineTo(4.93f, 14.89f)
            curveTo(3.99f, 14.02f, 3.93f, 12.98f, 3.93f, 11.9f)
            curveTo(3.93f, 7.43f, 7.56f, 3.8f, 12.04f, 3.8f)
            curveTo(16.51f, 3.8f, 20.14f, 7.43f, 20.14f, 11.9f)
            curveTo(20.14f, 16.38f, 16.51f, 18.48f, 12.04f, 18.48f)
            close()
            moveTo(16.49f, 12.4f)
            curveTo(16.25f, 12.28f, 15.04f, 11.68f, 14.81f, 11.6f)
            curveTo(14.59f, 11.52f, 14.42f, 11.48f, 14.26f, 11.72f)
            curveTo(14.1f, 11.96f, 13.63f, 12.52f, 13.48f, 12.68f)
            curveTo(13.34f, 12.84f, 13.19f, 12.86f, 12.95f, 12.74f)
            curveTo(12.71f, 12.62f, 11.93f, 12.36f, 11f, 11.53f)
            curveTo(10.28f, 10.89f, 9.79f, 10.09f, 9.65f, 9.85f)
            curveTo(9.51f, 9.61f, 9.64f, 9.48f, 9.76f, 9.36f)
            curveTo(9.87f, 9.25f, 10f, 9.07f, 10.13f, 8.93f)
            curveTo(10.25f, 8.79f, 10.29f, 8.69f, 10.37f, 8.53f)
            curveTo(10.45f, 8.37f, 10.41f, 8.22f, 10.35f, 8.1f)
            curveTo(10.29f, 7.98f, 9.8f, 6.77f, 9.59f, 6.28f)
            curveTo(9.39f, 5.8f, 9.19f, 5.87f, 9.04f, 5.86f)
            lineTo(8.57f, 5.86f)
            curveTo(8.41f, 5.86f, 8.14f, 5.92f, 7.92f, 6.16f)
            curveTo(7.7f, 6.4f, 7.06f, 7f, 7.06f, 8.21f)
            curveTo(7.06f, 9.42f, 7.94f, 10.59f, 8.07f, 10.76f)
            curveTo(8.19f, 10.92f, 9.8f, 13.4f, 12.26f, 14.46f)
            curveTo(12.85f, 14.71f, 13.3f, 14.87f, 13.66f, 14.98f)
            curveTo(14.25f, 15.17f, 14.79f, 15.14f, 15.22f, 15.08f)
            curveTo(15.7f, 15.01f, 16.67f, 14.49f, 16.87f, 13.92f)
            curveTo(17.07f, 13.35f, 17.07f, 12.86f, 17.01f, 12.76f)
            curveTo(16.95f, 12.66f, 16.73f, 12.52f, 16.49f, 12.4f)
            close()
        }
    }.build()
}

@Preview(showBackground = true)
@Composable
fun BookingSuccessDialogPreview() {
    BookingSuccessContent()
}
