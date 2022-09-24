package androidx.compose.material.icons.generator

import androidx.compose.material.icons.generator.vector.PathNode
import androidx.compose.material.icons.generator.vector.Vector
import androidx.compose.material.icons.generator.vector.VectorNode
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

data class PathAssetGenerationResult(
    val sourceGeneration: FileSpec, val accessProperty: String
)

/**
 * Derived from [VectorAssetGenerator], but only produces the desired path rather than rendering
 * it into an asset.
 *
 * @param iconName the name for the generated property, which is also used for the generated file.
 * I.e if the name is `Menu`, the property will be `Menu` (inside a theme receiver object) and
 * the file will be `Menu.kt` (under the theme package name).
 * @param iconGroupPackage the group that this vector belongs to. Used to scope the property to the
 * correct receiver object, and also for the package name of the generated file.
 * @param vector the parsed vector to generate VectorAssetBuilder commands for
 */
class PathAssetGenerator(
    private val iconName: String,
    private val iconGroupPackage: String,
    private val vector: Vector
) {
    /**
     * @return a [FileSpec] representing a Kotlin source file containing the property for this
     * programmatic [vector] representation.
     *
     * The package name and hence file location of the generated file is:
     * [PackageNames.MaterialIconsPackage] + [IconTheme.themePackageName].
     */
    fun createFileSpec(groupClassName: ClassName): PathAssetGenerationResult {
        val generation = FileSpec.builder(
            packageName = iconGroupPackage,
            fileName = iconName
        ).addProperty(
            PropertySpec.builder(name = iconName, type = List::class.asClassName().parameterizedBy(ClassNames.Path))
                .receiver(groupClassName)
                .delegate(
                    CodeBlock.Builder()
                        .beginControlFlow("lazy")
                        .add(
                            buildCodeBlock {
                                addStatement("listOf(")
                                indent()
                                vector.nodes.forEach { node -> addRecursively(node) }
                                unindent()
                                addStatement(")")
                            }
                        )
                        .endControlFlow()
                        .build()
                )
                .build()
        ).setIndent().build()

        return PathAssetGenerationResult(generation, iconName)
    }
}

private fun PathNode.pathFunction(): String =
    when (this) {
        is PathNode.CurveTo -> "cubicTo(${x1}f, ${y1}f, ${x2}f, ${y2}f, ${x3}f, ${y3}f)"
        is PathNode.QuadTo -> "quadraticBezierTo(${x1}f, ${y1}f, ${x2}f, ${y2}f)"
        is PathNode.RelativeCurveTo -> "relativeCubicTo(${dx1}f, ${dy1}f, ${dx2}f, ${dy2}f, ${dx3}f, ${dy3}f)"
        is PathNode.RelativeQuadTo -> "relativeQuadraticBezierTo(${x1}f, ${y1}f, ${x2}f, ${y2}f)"
        is PathNode.RelativeLineTo -> "relativeLineTo(${x}f, ${y}f)"
        is PathNode.RelativeMoveTo -> "relativeMoveTo(${x}f, ${y}f)"

        is PathNode.RelativeHorizontalTo,
        is PathNode.VerticalTo,
        is PathNode.RelativeVerticalTo,
        is PathNode.HorizontalTo,
        is PathNode.ArcTo,
        is PathNode.RelativeArcTo,
        is PathNode.ReflectiveCurveTo,
        is PathNode.ReflectiveQuadTo,
        is PathNode.RelativeReflectiveCurveTo,
        is PathNode.RelativeReflectiveQuadTo -> TODO()

        is PathNode.Close,
        is PathNode.LineTo,
        is PathNode.MoveTo -> this.asFunctionCall()
    }

/**
 * Recursively adds function calls to construct the given [vectorNode] and its children.
 */
private fun CodeBlock.Builder.addRecursively(vectorNode: VectorNode) {
    when (vectorNode) {
        // TODO: b/147418351 - add clip-paths once they are supported
        is VectorNode.Group -> {
            vectorNode.paths.forEach { path ->
                addRecursively(path)
            }
        }
        is VectorNode.Path -> {
            beginControlFlow("Path().apply")
            vectorNode.nodes.forEach { pathNode ->
                addStatement(pathNode.pathFunction())
            }
            unindent()
            add("},\n")
        }
    }
}
