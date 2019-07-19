package util


fun CmdRunner.rm_f(tmpFiles: List<String>)
{
    val cmd ="rm -f ${tmpFiles.joinToString(" ")}"
    this.run(cmd)
}
fun human_readable_number(num:Int):String {
    var number = num
    val units:Array<String> = arrayOf("","K","M","G","T","P")
    for(d in units){
        if(Math.abs(number) < 1000)
        {
            return "${number}${d}"
        }
        number = number /1000
    }
    return "${number}E"
}