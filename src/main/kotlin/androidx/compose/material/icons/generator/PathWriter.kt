package androidx.compose.material.icons.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import java.io.File

class PathWriter(
    private val icons: Collection<Icon>,
    private val groupClass: ClassName,
    private val groupPackage: String,
) {
    /**
     * Generates code for paths and writes them to [outputSrcDirectory], using [namePredicate] to
     * filter what icons to generate for.
     *
     * @param outputSrcDirectory the directory to generate source files in
     * @param namePredicate the predicate that filters what paths should be generated. If
     * false, the icon will not be parsed and generated in [outputSrcDirectory].
     *
     * @return MemberName of the created icons
     */
    fun generateTo(
        outputSrcDirectory: File,
        namePredicate: (String) -> Boolean
    ): List<MemberName> {

        return icons.filter { icon ->
            val iconName = icon.kotlinName

            namePredicate(iconName)
        }.map { icon ->
            val iconName = icon.kotlinName

            val vector = IconParser(icon).parse()

            val (fileSpec, accessProperty) = PathAssetGenerator(
                iconName,
                groupPackage,
                vector
            ).createFileSpec(groupClass)

            fileSpec.writeTo(outputSrcDirectory)

            MemberName(fileSpec.packageName, accessProperty)
        }
    }
}