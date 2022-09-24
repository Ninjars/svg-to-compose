package androidx.compose.material.icons.generator.util

import com.squareup.kotlinpoet.*

/**
 * @return The private backing property that is used to cache the VectorAsset for a given
 * icon once created.
 *
 * @param name the name of this property
 */
internal fun backingPropertySpec(name: String, type: TypeName): PropertySpec {
    val nullableVectorAsset = type.copy(nullable = true)
    return PropertySpec.builder(name = name, type = nullableVectorAsset)
        .mutable()
        .addModifiers(KModifier.PRIVATE)
        .initializer("null")
        .build()
}

internal inline fun FunSpec.Builder.withBackingProperty(
    backingProperty: PropertySpec,
    block: FunSpec.Builder.() -> Unit
): FunSpec.Builder = apply {
    beginControlFlow("if (%N == null)", backingProperty)
        .apply(block)
        .endControlFlow()
        .addStatement("return %N!!", backingProperty)
}

internal inline fun FunSpec.Builder.withBackingPropertyAndConstructor(
    backingProperty: PropertySpec,
    block: FunSpec.Builder.() -> Unit
): FunSpec.Builder = apply {
    addCode(buildCodeBlock {
        beginControlFlow("return if ($backingProperty != null)")
        addStatement("$backingProperty!!")
        endControlFlow()
    })
        .apply(block)
        .addStatement(".also { $backingProperty = it }")
}