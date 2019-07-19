package step
import util.*
import java.nio.file.*
import util.CmdRunner
import java.io.File
import kotlin.math.max

fun CmdRunner.xcor(taFile:String,mitoChrName:String,subsample:Int,speak:Int,minRange:Int?,maxRange:Int?,nth:Int,chipSeqType:String?,pairedEnd:Boolean,outputPrefix:String,outDir: Path) {
    Files.createDirectories(outDir)
    // log.info('Subsampling TAGALIGN for xcor...')
    var ta_subsampled:String
    var tmpFiles = mutableListOf<String>() //Delete temp files at the end

    if(pairedEnd)
    {
        ta_subsampled = subsample_ta_pe(
                taFile, subsample, mitoChrName,true,  true, outDir,outputPrefix)
    }
    else {
        ta_subsampled = subsample_ta_se(
                taFile, subsample, mitoChrName,true,outDir,outputPrefix)
    }
    tmpFiles.add(ta_subsampled)
    xcorFun(ta_subsampled,mitoChrName,subsample,speak,minRange,maxRange,nth,chipSeqType,pairedEnd,outputPrefix,outDir)
    rm_f(tmpFiles)

}
fun CmdRunner.xcorFun(taFile:String,mitoChrName:String,subsample:Int,speak:Int,minRange:Int?,maxRange:Int?,nth:Int,chipSeqType:String?,pairedEnd:Boolean,outputPrefix:String,outDir: Path) {
    /*subsample*/
    var prefix = outDir.resolve(outputPrefix)
    val xcor_plot_pdf = "${prefix}.cc.plot.pdf"//.format(prefix)
    val xcor_score = "${prefix}.cc.qc"//.format(prefix)
    val fraglen_txt = "${prefix}.cc.fraglen.txt"//.format(prefix)
    var exclusion_range_param:String
    var exclusion_range_max = maxRange
    var exclusion_range_min = minRange
    if(chipSeqType!=null && minRange!==null)
    {
        if(maxRange==null)
        {
            exclusion_range_max = get_exclusion_range_max(taFile,chipSeqType)
        }
        exclusion_range_param = "-x=${exclusion_range_min}:${exclusion_range_max}"

    }else {
        exclusion_range_param = ""
    }
    var s:String
    if(speak>=0){
        s=" -speak=${speak} "
    }else {
        s=""
    }
    var cmd1 = "Rscript --max-ppsize=500000 /run_spp.R -rf -c=${taFile} -p=${nth} "
    cmd1 += "-filtchr=\"${mitoChrName}\" -savp=${xcor_plot_pdf} -out=${xcor_score} ${s} "
    cmd1 += exclusion_range_param

    this.run(cmd1)

    val cmd2 = "sed -r \'s/,[^\\t]+//g\' -i ${xcor_score}"
    this.run(cmd2)

    // parse xcor_score and write fraglen (3rd column) to file
    var cmd3 = "echo ${parseXcorScore(xcor_score)["est_frag_len"]} > ${fraglen_txt}"
    this.run(cmd3)
}
fun parseXcorScore(filePath:String):Map<String, Any> {
    val result=mutableMapOf<String, Any>()
    var lines =File(filePath).readLines()
    var arr = lines[0].trim().split("\t")
    result["num_reads"] = (arr[1]).toInt()
    result["est_frag_len"] = (arr[2]).toInt()
    result["corr_est_frag_len"] = (arr[3]).toFloat()
    result["phantom_peak"] = (arr[4]).toInt()
    result["corr_phantom_peak"] = (arr[5]).toFloat()
    result["argmin_corr"] = (arr[6]).toInt()
    result["min_corr"] = (arr[7]).toFloat()
    result["NSC"] = (arr[8]).toFloat()
    result["RSC"] = (arr[9]).toFloat()
    return result
}

fun CmdRunner.get_exclusion_range_max(taFile:String,chipSeqType: String):Int {
    var cmd = "zcat -f ${taFile} > ${taFile}.tmp"
    this.run(cmd)

    var cmd1 = "head -n 100 ${taFile}.tmp | awk \'function abs(v) "
    cmd1 += "{{return v < 0 ? -v : v}} BEGIN{{sum=0}} "
    cmd1 += "{{sum+=abs($3-$2)}} END{{print int(sum/NR)}}\'"

    var lc = this.runCommand(cmd1)
    if(chipSeqType==="tf")
    {
        return max(lc!!.trim().toInt() + 10, 50)
    }else if(chipSeqType==="histone")
    {
        return max(lc!!.trim().toInt() + 10, 100)
    }else {
        throw Exception("Invalid Chip Seq Type")
    }
}
fun CmdRunner.subsample_ta_se(ta:String, subsample:Int,mito_chr_name:String, non_mito:Boolean, outDir:Path,outputPrefix:String):String{
    val prefix = outDir.resolve(outputPrefix)
    var nm:String
    var s:String
    if(non_mito)
    {
        nm="no_chrM."
    } else {
        nm=""

    }
    if(subsample>0)
    {
        s = human_readable_number(subsample)+"."
    } else {
        s=""
    }

    val ta_subsampled = "${prefix}.${nm}${s}tagAlign.gz"
    var cmd = "bash -c \"zcat -f ${ta} |"
    if(non_mito){
        cmd += "grep -v \'^${mito_chr_name}\\b\' | "
    }

    if(subsample>0){
        cmd += "shuf -n ${subsample} --random-source=<(openssl enc -aes-256-ctr -pass pass:$(zcat -f ${ta} | wc -c) -nosalt </dev/zero 2>/dev/null) | "
        cmd += "gzip -nc > ${ta_subsampled}\""
    }
    else {
        cmd += "gzip -nc > ${ta_subsampled}\""
    }

    this.run(cmd)
     return ta_subsampled
}
fun CmdRunner.subsample_ta_pe(ta:String, subsample:Int,mito_chr_name:String, non_mito:Boolean,r1_only:Boolean, outDir:Path,outputPrefix:String):String {
    val prefix = outDir.resolve(outputPrefix)
    var nm:String
    var s:String
    var r:String
    if(r1_only)
    {
        r="R1."
    } else {
        r=""

    }
    if(non_mito)
    {
        nm="no_chrM."
    } else {
        nm=""

    }
    if(subsample>0)
    {
        s = human_readable_number(subsample)+"."
    } else {
        s=""
    }
    val ta_subsampled = "${prefix}.${nm}${r}${s}tagAlign.gz"
    val ta_tmp = "${prefix}.tagAlign.tmp"
    var cmd = "bash -c \"zcat -f ${ta} |"
    if(non_mito){
        //# cmd += 'awk \'{{if ($1!="'+mito_chr_name+'") print $0}}\' | '
        cmd +="grep -v \'^\'${mito_chr_name}\'\' | "
    }
    cmd += "sed \'N;s/\\n/\\t/\'"
    if(subsample>0){
        cmd += " |  shuf -n ${subsample} --random-source=<(openssl enc -aes-256-ctr -pass pass:$(zcat -f ${ta} | wc -c) -nosalt </dev/zero 2>/dev/null) > ${ta_tmp}\" "
        // cmd += " > ${ta_tmp}\""
    }
    else {
        cmd += " > ${ta_tmp}\""
    }
    this.run(cmd)
    var cmd1 = "cat ${ta_tmp} | "
    cmd1 += "awk \'BEGIN{{OFS'\\t'}} "
    if(r1_only){
        //    cmd2 += "{{printf \"%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\n\","
        cmd1 += "{{printf \"%s\\t%s\\t%s\\t%s\\t%s\\t%s\\n\","
        cmd1 += "$1,$2,$3,$4,$5,$6}}\' | "
    }

    else {
        cmd1 += "{{printf \"%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\n\","
        cmd1 += "$1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12}}\' | "
    }
    cmd1 += "gzip -nc > ${ta_subsampled}"

    this.run(cmd1)
    rm_f(listOf(ta_tmp))
    return ta_subsampled
}
