package com.android.foodhub_android.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.foodhub_android.R
import com.android.foodhub_android.ui.features.auth.BaseAuthViewModel
import com.android.foodhub_android.ui.theme.Orange

@Composable
fun GroupSocialButtons(
    color : Color = Color.White,
    viewModel: BaseAuthViewModel
){
    Column {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                thickness = 1.dp,
                color = color
            )
            Text(text = stringResource(
                R.string.sign_in_title),
                color = color,
                modifier = Modifier.padding(8.dp)
            )

            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                thickness = 1.dp,
                color = color
            )
        }
        val context = LocalContext.current as ComponentActivity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            SocialButton(
                icon = R.drawable.ic_facebook,
                title = R.string.sign_with_facebook,
                onClick = {viewModel.onFacebookClicked(context)}
            )

            SocialButton(
                icon = R.drawable.ic_google,
                title = R.string.sign_with_google,
                onClick = {viewModel.onGoogleSignInClicked(context)}
            )
        }

    }
}


@Composable
fun SocialButton(
    icon : Int,
    title : Int,
    onClick : () -> Unit
){
    Button(
        onClick = onClick,
        colors = buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(32.dp)
    ){
        Row(
            modifier = Modifier.height(38.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Image(
                painter = painterResource(id = icon),
                contentDescription = "Google Icon",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text=stringResource(id = title),
                color = Color.Black
            )
        }
    }
}

@Composable
fun FoodHubTextField(value: String,
                     onValueChange: (String) -> Unit,
                     modifier: Modifier = Modifier,
                     enabled: Boolean = true,
                     readOnly: Boolean = false,
                     textStyle: TextStyle = LocalTextStyle.current,
                     label: @Composable (() -> Unit)? = null,
                     placeholder: @Composable (() -> Unit)? = null,
                     leadingIcon: @Composable (() -> Unit)? = null,
                     trailingIcon: @Composable (() -> Unit)? = null,
                     prefix: @Composable (() -> Unit)? = null,
                     suffix: @Composable (() -> Unit)? = null,
                     supportingText: @Composable (() -> Unit)? = null,
                     isError: Boolean = false,
                     visualTransformation: VisualTransformation = VisualTransformation.None,
                     keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
                     keyboardActions: KeyboardActions = KeyboardActions.Default,
                     singleLine: Boolean = false,
                     maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
                     minLines: Int = 1,
                     interactionSource: MutableInteractionSource? = null,
                     shape: Shape = RoundedCornerShape(10.dp),
                     colors: TextFieldColors = OutlinedTextFieldDefaults.colors().copy(
                         focusedIndicatorColor = Orange,
                         unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.4f)
                     )
){
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        label?.let{
            Row{
                Spacer(modifier = Modifier.size(4.dp))
                it()
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        OutlinedTextField(
            value,
            onValueChange,
            modifier,
            enabled,
            readOnly,
            textStyle.copy(fontWeight = FontWeight.SemiBold),
            null,
            placeholder,
            leadingIcon,
            trailingIcon,
            prefix,
            suffix,
            supportingText,
            isError,
            visualTransformation,
            keyboardOptions,
            keyboardActions,
            singleLine,
            maxLines,
            minLines,
            interactionSource,
            shape,
            colors
        )
    }
}

@Composable
fun BasicDialog(title:String, description:String, onClick: () -> Unit){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = title,
            color = Color.Black,
            fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = description,
            color = Color.DarkGray)
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth()
        ){
            Text(text = stringResource(R.string.ok),
                color = Color.White)
        }
    }
}

fun LazyListScope.gridItems(
    count: Int,
    nColumns: Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    itemContent: @Composable BoxScope.(Int) -> Unit,
) {
    gridItems(
        data = List(count) { it },
        nColumns = nColumns,
        horizontalArrangement = horizontalArrangement,
        itemContent = itemContent,
    )
}

fun <T> LazyListScope.gridItems(
    data: List<T>,
    nColumns: Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable BoxScope.(T) -> Unit,
) {
    val rows = if (data.isEmpty()) 0 else 1 + (data.count() - 1) / nColumns
    items(rows) { rowIndex ->
        Row(horizontalArrangement = horizontalArrangement) {
            for (columnIndex in 0 until nColumns) {
                val itemIndex = rowIndex * nColumns + columnIndex
                if (itemIndex < data.count()) {
                    val item = data[itemIndex]
                    androidx.compose.runtime.key(key?.invoke(item)) {
                        Box(
                            modifier = Modifier.weight(1f, fill = true),
                            propagateMinConstraints = true
                        ) {
                            itemContent.invoke(this, item)
                        }
                    }
                } else {
                    Spacer(Modifier.weight(1f, fill = true))
                }
            }
        }
    }
}