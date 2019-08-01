import com.github.ajalt.clikt.core.CliktCommand

import com.github.ajalt.clikt.parameters.options.*

import com.github.ajalt.clikt.parameters.types.*

import mu.KotlinLogging

import step.*

import util.*

import java.nio.file.*

import util.CmdRunner

fun main(args: Array<String>) = Cli().main(args)



class Cli : CliktCommand() {

    private val outputPrefix: String by option("-outputPrefix", help = "output file name prefix; defaults to 'output'").default("idr")
    private val outDir by option("-outputDir", help = "path to output Directory")
            .path().required()
    private val taFile: Path by option("-ta", help = "path for TAGALIGN file ")
            .path().required()
    private val mitoChrName:String by option("-mito-chr-name",help = "Mito Chromosome Name").default("chrM")
    private val subsample: Int by option("-subsample", help = "subsample TagAlign").int().default(0)
    private val speak: Int by option("-speak", help = "User-defined cross-corr. peak strandshift (-speak= in run_spp.R). Disabled if -1.").int().default(-1)
    private val minRange: Int? by option("-exclusion-range-min", help = "User-defined exclusion range minimum used for ").int()
    private val maxRange: Int? by option("-exclusion-range-max", help = "User-defined exclusion range maximum used for ").int()
    private val chipSeqType:String? by option("-chip-seq-type",help = "Chip Seq Type Pipeline").choice("histone","tf").required()
    private val pairedEnd: Boolean by option("-pairedEnd", help = "Paired End").flag()
    private val parallelism: Int by option("-parallelism", help = "Number of threads to parallelize.").int().default(1)

    override fun run() {
        val cmdRunner = DefaultCmdRunner()
        cmdRunner.runTask(taFile,mitoChrName,subsample,speak,minRange,maxRange,parallelism,chipSeqType,pairedEnd,outputPrefix,outDir)
    }
}



/**

 * Runs pre-processing and bwa for raw input files

 */

fun CmdRunner.runTask(taFile:Path,mitoChrName:String,subsample:Int,speak:Int,minRange:Int?,maxRange:Int?,nth:Int,chipSeqType:String?,pairedEnd:Boolean,outputPrefix:String,outDir: Path) {

    xcor(taFile.toString(),mitoChrName,subsample,speak,minRange,maxRange,nth,chipSeqType,pairedEnd,outputPrefix,outDir)

}