package read_assessment.data

object ReadAssessmentSource {
    val piPaXing = "同是天涯沦落人，相逢何必曾相识！" +
            "我从去年辞帝京，谪居卧病浔阳城。浔阳地僻无音乐，终岁不闻丝竹声。住近湓江地低湿，黄芦苦竹绕宅生。"
    val changHenGe = "临别殷勤重寄词，词中有誓两心知。" +
            "七月七日长生殿，夜半无人私语时，在天愿作比翼鸟，在地愿为连理枝。天长地久有时尽，此恨绵绵无绝期。"
    val chunJiangHuaYueYe = "江畔何人初见月？江月何年初照人？" +
            "人生代代无穷已，江月年年望相似。不知江月待何人，但见长江送流水。白云一片去悠悠，青枫浦上不胜愁。"
    val hongLouMeng = "贾不假，白玉为堂金作马；阿房宫，三百里，住不下金陵一个史；" +
            "东海缺少白玉床，龙王来请金陵王；丰年好大雪，珍珠如土金如铁。"
    val baiNianGuDu = "多年以后，面对行刑队，奥雷里亚诺·布恩迪亚上校将会回想起父亲带他去见识冰块的那个遥远的下午。"
    val heLiNeiLeDuo = "赫里内勒多·马尔克斯上校曾躲过三次暗杀，五次受伤大难不死，身经百战安然无恙，却败给了无尽的等待，" +
            "屈服于凄凉的晚景，在一间借来的光线昏暗的屋子里想着阿玛兰妲"
    val list: List<String> = listOf(
        piPaXing,
        changHenGe,
        chunJiangHuaYueYe,
        hongLouMeng,
        baiNianGuDu,
        heLiNeiLeDuo
    )
    fun getTextByIndex(index: Int): String {
        return list[index % list.size]
    }
}