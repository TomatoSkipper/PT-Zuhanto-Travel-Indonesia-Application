package com.example.appproject.ui.screens

import android.content.Intent
import android.graphics.PointF
import android.net.Uri
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.TextStyle
import coil.compose.AsyncImage
import com.example.appproject.R
import com.example.appproject.data.model.CoreValue
import com.example.appproject.data.model.Feature
import com.example.appproject.data.model.ManagementMember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import com.example.appproject.ui.theme.PrimaryBlue
import com.example.appproject.ui.theme.PrimaryBlueDark
import com.example.appproject.ui.theme.WarmAmberGold
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Facebook
import compose.icons.fontawesomeicons.brands.Instagram
import compose.icons.fontawesomeicons.brands.Tiktok

@Composable
fun AboutUsScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isWideLayout = adaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT
    val whatsappUrl = "https://api.whatsapp.com/send/?phone=628133315225&text=Hello+PT+ZUHANTO+TRAVEL+INDONESIA%2C+I+would+like+to+inquire+about+tour+packages+and+date+availability.+Thank+you.&type=phone_number&app_absent=0"
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val coreValues = listOf(
        CoreValue(stringResource(R.string.integrity), stringResource(R.string.integrity_desc)),
        CoreValue(
            stringResource(R.string.customer_satisfaction),
            stringResource(R.string.customer_satisfaction_desc)
        ),
        CoreValue(stringResource(R.string.innovation), stringResource(R.string.innovation_desc)),
        CoreValue(
            stringResource(R.string.professionalism),
            stringResource(R.string.professionalism_desc)
        ),
        CoreValue(
            stringResource(R.string.sustainable_growth),
            stringResource(R.string.sustainable_growth_desc)
        )
    )

    val managementTeam = listOf(
        ManagementMember("Dr. Moch. Zuhan", stringResource(R.string.role_chairman)),
        ManagementMember("Sumanto", stringResource(R.string.role_ceo)),
        ManagementMember("Hengky Melkior Sopaheluwaken", stringResource(R.string.role_cmo)),
        ManagementMember("Muhammad Sholihuddin", stringResource(R.string.role_coo)),
        ManagementMember("Riski Diah Meylani", stringResource(R.string.role_marketing_exec)),
        ManagementMember("Ria Ayu Nova Aprillia", stringResource(R.string.role_admin_officer))
    )

    val whyChooseUs = listOf(
        Feature(
            stringResource(R.string.prof_expertise_experience),
            stringResource(R.string.prof_expertise_experience_desc),
            Icons.Default.WorkspacePremium
        ),
        Feature(
            stringResource(R.string.pricing_transparency),
            stringResource(R.string.pricing_transparency_desc),
            Icons.Default.AttachMoney
        ),
        Feature(
            stringResource(R.string.customer_support_24_7),
            stringResource(R.string.customer_support_desc),
            Icons.Default.SupportAgent
        ),
        Feature(
            stringResource(R.string.safety_commitment),
            stringResource(R.string.safety_commitment_desc),
            Icons.Default.Security
        ),
        Feature(
            stringResource(R.string.customized_solutions),
            stringResource(R.string.customized_solutions_desc),
            Icons.Default.Tune
        ),
        Feature(
            stringResource(R.string.trusted_by_corporate),
            stringResource(R.string.trusted_by_corporate_desc),
            Icons.Default.Business
        )
    )

    val serviceGuarantee = listOf(
        Feature(
            stringResource(R.string.quality_assurance),
            stringResource(R.string.quality_assurance_desc),
            Icons.Default.CheckCircle
        ),
        Feature(
            stringResource(R.string.full_transparency),
            stringResource(R.string.full_transparency_desc),
            Icons.Default.CheckCircle
        ),
        Feature(
            stringResource(R.string.prof_service_standard),
            stringResource(R.string.prof_service_standard_desc),
            Icons.Default.CheckCircle
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                    context.startActivity(intent)
                },
                containerColor = Color(0xFF25D366),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Phone, contentDescription = null) },
                text = { Text(text = stringResource(R.string.contact_us), fontWeight = FontWeight.Bold, maxLines = 2) }
            )
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                initialOffsetY = { 40 },
                animationSpec = tween(600, easing = EaseOutQuad)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                ) {
                    AsyncImage(
                        model = R.drawable.backgroudmountain,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.7f
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Black.copy(alpha = 0.9f)
                                    )
                                )
                            )
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp)
                            .size(42.dp)
                            .align(Alignment.TopStart),
                        onClick = onBack
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.size(100.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            AsyncImage(
                                model =  R.drawable.zti_logo,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.about_company),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = stringResource(R.string.company_slogan),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    offset = PointF(2f, 2f).let { Offset(it.x, it.y) },
                                    blurRadius = 4f
                                )
                            ),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.company_description_short),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.company_description_full),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Justify
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MissionVisionCard(
                        title = stringResource(R.string.our_mission),
                        description = stringResource(R.string.mission_description),
                        modifier = Modifier.weight(1f)
                    )
                    MissionVisionCard(
                        title = stringResource(R.string.our_vision),
                        description = stringResource(R.string.vision_description),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                SectionHeader(
                    title = stringResource(R.string.management_team),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                managementTeam.forEach { member ->
                    ManagementMemberItem(member = member)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(40.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(vertical = 40.dp, horizontal = 24.dp)
                ) {
                    SectionHeader(
                        title = stringResource(R.string.core_values),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    coreValues.forEach { value ->
                        CoreValueItem(value = value)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                SectionHeader(
                    title = stringResource(R.string.why_choose_us),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                whyChooseUs.forEach { feature ->
                    FeatureItem(feature = feature)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(40.dp))

                SectionHeader(
                    title = stringResource(R.string.service_guarantee),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                serviceGuarantee.forEach { feature ->
                    FeatureItem(feature = feature)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                ContactAndFooter(isWideLayout = isWideLayout)
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight = FontWeight.Bold,
    style: TextStyle = MaterialTheme.typography.headlineSmall
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment
    ) {
        Text(
            text = title,
            style = style,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.secondary)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ManagementMemberItem(member: ManagementMember) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = member.role,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FeatureItem(feature: Feature) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MissionVisionCard(title: String, description: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.heightIn(min = 180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (title.contains("Mission")) Icons.Default.TrackChanges else Icons.Default.Visibility,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CoreValueItem(value: CoreValue) {
    val icon = when (value.title) {
        stringResource(R.string.integrity) -> Icons.Default.VerifiedUser
        stringResource(R.string.customer_satisfaction) -> Icons.Default.SentimentVerySatisfied
        stringResource(R.string.innovation) -> Icons.Default.Lightbulb
        stringResource(R.string.professionalism) -> Icons.Default.WorkspacePremium
        stringResource(R.string.sustainable_growth) -> Icons.Default.TrendingUp
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = value.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = value.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ContactAndFooter(isWideLayout: Boolean) {
    val navy = PrimaryBlueDark
    val accent = WarmAmberGold
    val cyan = Color(0xFF38BDF8)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(navy)
            .padding(horizontal = if (isWideLayout) 64.dp else 20.dp, vertical = 40.dp)
    ) {
        if (isWideLayout) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContactDetails(modifier = Modifier.weight(1f), accent = accent, cyan = cyan)
                ContactPhoto(modifier = Modifier.weight(1f))
            }
        } else {
            ContactDetails(accent = accent, cyan = cyan)
            Spacer(modifier = height(24.dp))
            ContactPhoto()
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 36.dp, bottom = 24.dp),
            color = accent.copy(alpha = 0.5f)
        )

        if (isWideLayout) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                FooterBrand(modifier = Modifier.weight(1.3f), accent = accent, cyan = cyan)
                FooterContact(modifier = Modifier.weight(1.1f), accent = accent, cyan = cyan)
                FooterSocials(modifier = Modifier.weight(0.8f), accent = accent)
            }
        } else {
            FooterBrand(accent = accent, cyan = cyan)
            Spacer(modifier = height(20.dp))
            FooterContact(accent = accent, cyan = cyan)
            Spacer(modifier = height(20.dp))
            FooterSocials(accent = accent)
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 28.dp, bottom = 16.dp),
            color = Color.White.copy(alpha = 0.15f)
        )
        Text(
            text = stringResource(R.string.copyright_text),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.65f)
        )
    }
}

@Composable
fun ContactDetails(modifier: Modifier = Modifier, accent: Color, cyan: Color) {
    Column(modifier = modifier) {
        Text(stringResource(R.string.start_your), style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Serif, color = Color.White)
        Text(stringResource(R.string.dream_journey), style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, color = accent)
        Box(modifier = Modifier.padding(vertical = 10.dp).width(50.dp).height(3.dp).background(cyan, RoundedCornerShape(2.dp)))
        Text(stringResource(R.string.contact_desc), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = height(16.dp))
        FooterLine(Icons.Default.Place, stringResource(R.string.head_office), stringResource(R.string.head_office_location), cyan)
        FooterLine(Icons.Default.Phone, stringResource(R.string.phone_transalate), "+62 813 3315 225", cyan)
        FooterLine(Icons.Default.Email, stringResource(R.string.email_transalate), "info@ztiindo.id", cyan)
        FooterLine(Icons.Default.Language, stringResource(R.string.website_transalate), "www.ztiindo.id", cyan)
    }
}

@Composable
fun ContactPhoto(modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(240.dp), shape = RoundedCornerShape(16.dp)) {
        AsyncImage(model = R.drawable.officeimage, contentDescription = "Zuhanto Travel office", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}

@Composable
fun FooterBrand(modifier: Modifier = Modifier, accent: Color, cyan: Color) {
    Column(modifier = modifier) {
        AsyncImage(model = R.drawable.zti_logo, contentDescription = "Zuhanto Travel logo", modifier = size(42.dp).background(Color.White, CircleShape).padding(6.dp), contentScale = ContentScale.Crop)
        Spacer(modifier = height(10.dp))
        Text("PT ZUHANTO TRAVEL\nINDONESIA", fontWeight = FontWeight.Bold, color = accent, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = height(8.dp))
        Text(stringResource(R.string.pt_zuhanto_desc), color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun FooterContact(modifier: Modifier = Modifier, accent: Color, cyan: Color) {
    Column(modifier = modifier) {
        Text(stringResource(R.string.contact_us_transalate), color = accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = height(12.dp))
        FooterLine(Icons.Default.Place, "", stringResource(R.string.place), cyan)
        FooterLine(Icons.Default.Phone, "", "+62 812 333 951–165", cyan)
        FooterLine(Icons.Default.Email, "", "info@ztiindo.id", cyan)
    }
}

@Composable
fun FooterSocials(modifier: Modifier = Modifier, accent: Color) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(stringResource(R.string.follow_us), color = accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val socialLinks = listOf(
                Triple("Instagram", FontAwesomeIcons.Brands.Instagram, "https://www.instagram.com/zuhantotravelindo"),
                Triple("Facebook", FontAwesomeIcons.Brands.Facebook, "https://www.facebook.com/share/1MH9Q8RGc8/"),
                Triple("TikTok", FontAwesomeIcons.Brands.Tiktok, "https://www.tiktok.com/@zuhantotravelindo")
            )
            socialLinks.forEach { (name, icon, url) ->
                Surface(
                    modifier = size(32.dp)
                        .clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Open $name",
                            tint = Color.White,
                            modifier = size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FooterLine(icon: ImageVector, label: String, value: String, tint: Color) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = size(16.dp), tint = tint)
        Spacer(modifier = width(8.dp))
        Column {
            if (label.isNotEmpty()) Text(label, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            Text(value, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CategoryItem(
    icon: ImageVector,
    label: String,
    iconSize: Dp = 52.dp,
    onClick: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = padding(6.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .motionEventSpy { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> isPressed = true
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isPressed = false
                }
            }
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f)),
            modifier = Modifier.size(iconSize),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(iconSize / 2.2f)
                )
            }
        }
        Spacer(modifier = height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ServiceItem(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Action */ }
            )
            .motionEventSpy { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> isPressed = true
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isPressed = false
                }
            },
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = padding(14.dp).fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = PrimaryBlue,
                        modifier = size(20.dp)
                    )
                }
            }
            Spacer(modifier = height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                maxLines = 2,
                lineHeight = 18.sp
            )
            Spacer(modifier = height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                lineHeight = 16.sp
            )
        }
    }
}