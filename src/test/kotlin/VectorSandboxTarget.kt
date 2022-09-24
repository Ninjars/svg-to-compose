package br.com.devsrsouza.svg2compose

import java.io.File


fun main(){
    val iconTest = File("E:\\Jez\\Documents\\dev\\VectorSandbox\\assets")
    val src = File("E:\\Jez\\Documents\\dev\\VectorSandbox\\app\\src\\main\\java\\jez\\vectorsandbox").apply { mkdirs() }

    Svg2Compose.parse(
        applicationIconPackage = "vectors",
        accessorName = "VectorAssets",
        outputSourceDirectory = src,
        vectorsDirectory = iconTest,
        inputType = VectorType.SVG,
        outputType = OutputType.PATH,
        allAssetsPropertyName = "VectorAssets"
    )
}
