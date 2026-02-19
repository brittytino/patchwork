package com.brittytino.patchwork.ui.components.dialogs

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.brittytino.patchwork.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.res.stringResource

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AboutSection(
    modifier: Modifier = Modifier,
    appName: String = stringResource(R.string.app_name),
    developerName: String = stringResource(R.string.app_developer_name),
    description: String = stringResource(R.string.app_description),
    onAvatarLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (_: Exception) {
        "Unknown"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceBright
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "$appName v$versionName", style = MaterialTheme.typography.headlineLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

            Image(
                painter = painterResource(id = R.drawable.avatar),
                contentDescription = "Developer Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { 
                            onAvatarLongClick()
                        }
                    )
            )

            Text(text = stringResource(R.string.developed_by_format, developerName), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 3
            ) {
                Button(
                    onClick = {
                        val websiteUrl = "https://tinobritty.me"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_web_traffic_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_website))
                }

                Button(
                    onClick = {
                        val websiteUrl = "https://github.com/brittytino/patchwork"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_globe_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_view_on_github))
                }

                OutlinedButton(
                    onClick = {
                        // Use mailto: URI so the system opens an email client
                        val mailUri = "mailto:mail@brittytino08@gmail.com".toUri()
                        val emailIntent = Intent(Intent.ACTION_SENDTO, mailUri).apply {
                            putExtra(Intent.EXTRA_SUBJECT, "Hello from Essentials")
                        }
                        try {
                            context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.send_email_chooser_title)))
                        } catch (e: ActivityNotFoundException) {
                            Log.w("AboutSection", "No email app available", e)
                            Toast.makeText(context, R.string.error_no_email_app, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_mail_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_contact))
                }

                OutlinedButton(
                    onClick = {
                        val websiteUrl = "https://t.me/tidwib"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_globe_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_telegram))
                }

                OutlinedButton(
                    onClick = {
                        val websiteUrl = "https://buymeacoffee.com/brittytino"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_heart_smile_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_support))
                }
            }
        }
    }
}
