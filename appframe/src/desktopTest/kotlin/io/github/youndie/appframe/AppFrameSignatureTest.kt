package io.github.youndie.appframe

import androidx.compose.runtime.Composer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.WindowState
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertTrue

class AppFrameSignatureTest {
    /**
     * Code compiled against 0.3.0 calls `AppFrameKt.AppFrame-AlbwjTQ` with exactly this descriptor,
     * read with `javap -s` off `appframe-desktop-0.3.0.jar` from Maven Central. Every parameter
     * added to `AppFrame` since changes the new method's descriptor, so the old one has to stay —
     * without it such code fails with `NoSuchMethodError`, the way 0.2.0's did under 0.3.0
     * (CHANGELOG.md, research §1.4).
     */
    @Test
    fun `the 0_3_0 AppFrame is still there for code compiled against it`() {
        val frame = Class.forName("io.github.youndie.appframe.AppFrameKt")

        val method =
            frame.getDeclaredMethod(
                "AppFrame-AlbwjTQ",
                Function0::class.java, // onCloseRequest
                WindowState::class.java,
                Boolean::class.javaPrimitiveType, // visible
                String::class.java, // title
                Painter::class.java, // icon
                Boolean::class.javaPrimitiveType, // resizable
                Float::class.javaPrimitiveType, // resizeBorder: Dp
                Boolean::class.javaPrimitiveType, // alwaysOnTop
                TitleBarStyle::class.java,
                Function1::class.java, // onPreviewKeyEvent
                Function1::class.java, // onKeyEvent
                Function3::class.java, // menuBar
                Function3::class.java, // actions
                Function3::class.java, // content
                Composer::class.java,
                Int::class.javaPrimitiveType, // $changed
                Int::class.javaPrimitiveType, // $changed1
                Int::class.javaPrimitiveType, // $default
            )

        assertTrue(Modifier.isPublic(method.modifiers) && Modifier.isStatic(method.modifiers))
        assertTrue(method.returnType == Void.TYPE)
    }
}
