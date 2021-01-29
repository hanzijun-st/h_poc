package com.qianlima.offline.rule02;

import com.qianlima.offline.bean.ConstantBean;
import org.apache.commons.lang3.StringUtils;

public class BusinessRule {

    private static String[] a = {  "郑煤", "晋煤", "矿业" ,"煤矿" ,"煤业" ,"煤炭" ,"精煤" ,"焦煤" ,"矿产" ,"矿物" ,"铁矿" ,"铜矿" ,"矿务" ,"选矿" ,"矿山" ,"地质" ,"采砂" ,"兖矿"};
    private static String[] b = { "北京国电", "国电南瑞", "粤电", "电务公司", "电力" ,"电场" ,"电厂" ,"电网" ,"发电" ,"国电集团" ,"供电" ,"煤电" ,"水电" ,"核电" ,"电能" ,"风电" ,"电站" ,"热电" ,"电建" ,"华电集团" ,"大唐集团" };
    private static String[] c = { "电气" ,"自动化" ,"机电" ,"弱电" ,"仪表" ,"仪器" ,"航电" ,"光电"};
    private static String[] d = { "燃气" ,"煤气" ,"天然气" ,"热力" ,"供热" ,"供暖" ,"供气", "油气", "地热", "燃料"};
    private static String[] e = { "石油" ,"石化" ,"海油" ,"钻井" ,"化工" ,"化学" ,"钻探", "化纤"};
    private static String[] f = { "水处理", "制水", "水利" ,"自来水" ,"供水" ,"水务" ,"水场" ,"矿泉水" ,"排水" ,"水环境" ,"污水处理" ,"饮用水" ,"水资源" ,"污水净化"};
    private static String[] g = { "管网" ,"管道" ,"光纤" ,"电缆" ,"管线" ,"管业", "管路"};
    private static String[] h = { "能源" ,"能耗" ,"能效" ,"节能" ,"风能" ,"地热能" ,"潮汐能" ,"太阳能" ,"电池", "核能", "中核", "中广核"};
    private static String[] i = { "邮政集团", "空运", "陆运", "速运", "速递", "送货", "配送", "搬运", "装卸", "邮政公司", "物流" ,"仓储" ,"仓库" ,"运输" ,"快递" ,"海运" ,"顺丰" ,"中通" ,"圆通" ,"快运" ,"集装箱"};
    private static String[] j = { "东航","机场" ,"港口" ,"航空" ,"飞机" ,"港务" ,"船舶" ,"民航" ,"码头" ,"南航" ,"船厂" ,"轮船" ,"船务", "航运", "轮渡"};
    private static String[] k = { "轨道" ,"地铁" ,"铁路" ,"有轨电车" ,"高铁", "电车", "火车", "中铁"};
    private static String[] l = { "公共交通" ,"公路" ,"交通" ,"公交" ,"客运", "巴士", "停车场", "停车管理", "停车服务", "中交", "停车运营", "大桥", "桥路", "停车产业"};
    private static String[] m = { "海尔集团", "铝业", "制品", "润滑油", "加工" ,"装备" ,"重工" ,"轻工" ,"纺织" ,"钢铁" ,"型材" ,"板材" ,"柴油机" ,"不锈钢" ,"电器" ,"材料" ,"家具" ,"机械" ,"制造" ,"空调" ,"印刷" ,"纸业" ,"工业" ,"云南云铝" ,"铜业" ,"锡业" ,"精密铸造" ,"轮胎" ,"电梯" ,"橡胶"};
    private static String[] n = { "便利", "酒水", "酒类", "酒业", "日化", "美妆", "化妆品", "咖啡", "酒股份", "零售" ,"销售" ,"超市" ,"商贸" ,"贸易" ,"饮料" ,"批发" ,"服装" ,"食品" ,"餐饮" ,"连锁" ,"快消品" ,"商场" ,"进出口" ,"粮油" ,"储备粮" ,"糖业" ,"贵州茅台" ,"日用品" ,"粮食" ,"办公用品" ,"乳业"};
    private static String[] o = { "电子科技", "信息科技", "智能" ,"智慧" ,"机器人" ,"无人机" ,"航发动力" ,"科学技术" ,"科技" ,"航天科工" ,"中国航发" ,"发动机"};
    private static String[] p = { "烟草" ,"中烟" ,"烟叶" ,"卷烟" ,"香烟", "烟滤嘴", "电子烟", "烟嘴"};
    private static String[] q = { "汽车" ,"电动车" ,"轿车" ,"商用车" ,"客车" ,"越野车" ,"特种车" ,"工业车" ,"出租车" ,"机动车" ,"车辆" ,"网约车" ,"旅游车" ,"二手车" ,"共享车" ,"重汽"};
    private static String[] r = { "消防" ,"安防" ,"安检" ,"保安" ,"安保" ,"安全保卫", "押运"};
    private static String[] s = { "手机" ,"信号" ,"基站" ,"通信" ,"通讯" ,"信息技术" ,"信息科技" ,"信息安全" ,"信息网络" ,"信息产业" ,"有线网络" ,"有限网络" ,"无线电"};
    private static String[] t = { "网络股份", "网络信息", "互联网" ,"电子商务" ,"电商" ,"数据" ,"云计算" ,"物联科技" ,"车联网" ,"互联科技" ,"网络科技" ,"网络运行" ,"网络技术" ,"网络服务" ,"网络工程" ,"网络有限" ,"网络安全" ,"网络系统" ,"网格化" ,"数字技术" ,"数字化" ,"信息系统" ,"物联网" ,"云信息" ,"云安全"};
    private static String[] u = { "电信" ,"移动" ,"联通" ,"铁通" ,"联合通信" ,"联合网络通信" ,"铁塔", "中移"};
    private static String[] v = { "系统集成" ,"电子" ,"软件" ,"硬件" ,"计算机" ,"电脑" ,"芯片" ,"元器件" ,"集成电路" ,"路由器" ,"交换机" ,"服务器" ,"数码"};
    private static String[] w = { "环保" ,"垃圾" ,"环境治理" ,"环境保护" ,"环境服务" ,"环境产业", "生态资源", "生态环境"};
    private static String[] x = { "农业" ,"农场" ,"农产品" ,"化肥" ,"种子", "农贸"};
    private static String[] y = { "林业" ,"林场" ,"园林"};
    private static String[] z = { "渔业" ,"水产" ,"生鲜"};
    private static String[] a1 = { "畜牧" ,"牧业" ,"牧场" ,"农牧" ,"饲料"};
    private static String[] b1 = { "体育" ,"运动" ,"足球" ,"篮球" ,"羽毛球"};
    private static String[] c1 = { "文化产业", "文化传播" ,"图书馆" ,"出版" ,"书店" ,"图书" ,"文艺" ,"日报" ,"报业" ,"报社", "会展", "展览", "博览"};
    private static String[] d1 = { "旅游" ,"旅行" ,"度假" ,"饭店" ,"酒店" ,"客轮" ,"游轮" ,"游客" ,"景区" ,"文旅" ,"宾馆" ,"商旅"};
    private static String[] e1 = { "娱乐" ,"视频" ,"影视" ,"电视" ,"广播" ,"电影" ,"制片" ,"演艺" ,"演出", "传媒", "媒体", "广电", "广告", "人民网"};
    private static String[] f1 = { "教育" ,"辅导" ,"课程" ,"学习" ,"培训" ,"家教", "职教"};
    private static String[] g1 = { "生命", "健康", "国药", "生物科学", "基因", "医生", "生物工程", "细胞", "器官", "医疗" ,"药业" ,"医药" ,"医学" ,"体检" ,"医用" ,"生物技术" ,"制药" ,"药剂"};
    private static String[] h1 = { "建筑" ,"建设" ,"建工" ,"工程" ,"城建" ,"造价咨询", "中建"};
    private static String[] i1 = { "饰品", "窗帘", "水泥", "混凝土", "建材", "装饰" ,"装修", "灯饰", "装潢", "安装"};
    private static String[] j1 = { "招标采购", "招标代理", "物业管理" ,"保洁服务" ,"洗涤" ,"物业服务" ,"物业顾问" ,"清洁服务" ,"物业公司" ,"家政服务", "社区服务", "园区服务", "物业有限"};
    private static String[] k1 = { "地产" ,"房产" ,"房屋" ,"商品房" ,"置业" ,"二手房" ,"土地发展" ,"长租房", "不动产", "租房"};
    private static String[] l1 = { "公司" ,"股份" ,"集团"};
    private static String[] a11 = { "煤矿"};
    private static String[] b11 = { "电场" ,"电厂"};
    private static String[] e11 = { "油田", "油厂", "加油站", "采气厂"};
    private static String[] f11 = { "水厂" ,"水场"};
    private static String[] m11 = { "加工厂" ,"总厂" ,"工厂" ,"机械厂" ,"印刷厂" ,"设备厂" ,"织造厂" ,"邮票厂" ,"化肥厂" ,"修造厂" ,"器材厂" ,"钢铁厂" ,"炼铁厂", "材料厂", "水泥厂", "炼铁厂"};
    private static String[] n11 = { "超市" ,"商店" ,"专卖店" ,"专营店" ,"书店" ,"电子商行", "经营部", "酒厂"};
    private static String[] o11 = { "科学技术馆" ,"科技馆", "科技园"};
    private static String[] u11 = { "电信" ,"移动" ,"联通"};
    private static String[] x11 = { "农场" ,"茶场"};
    private static String[] bb11 = { "体育馆"};
    private static String[] ce11 = { "艺术馆" ,"美术馆" ,"图书馆" ,"书院", "展览馆", "出版社", "报社", "会展中心", "传播中心", "场馆"};
    private static String[] ee11 = { "电影院", "媒体中心"};
    private static String[] w11 = { "处理厂", "填埋场" };
    private static String[] p11 = { "卷烟厂" };
    private static String[] d11 = { "宾馆", "酒店", "饭店", "招待所" };

    private static String[] StrArrayName = { "商业公司-采矿", "商业公司-电力", "商业公司-电气", "商业公司-燃气热力", "商业公司-石油化工", "商业公司-水利", "商业公司-管网", "商业公司-新能源", "商业公司-物流仓储", "商业公司-机场港口", "商业公司-轨道交通", "商业公司-城市交通", "商业公司-制造", "商业公司-零售批发", "商业公司-智慧科技", "商业公司-烟草", "商业公司-汽车", "商业公司-消防安防", "商业公司-通信", "商业公司-互联网", "商业公司-运营商", "商业公司-系统集成", "商业公司-环保", "商业公司-农业", "商业公司-林业", "商业公司-渔业", "商业公司-畜牧", "商业公司-体育", "商业公司-文化", "商业公司-旅游", "商业公司-传媒", "商业公司-教育服务", "商业公司-医疗服务", "商业公司-工程建筑", "商业公司-装饰装修", "商业公司-生活服务","商业公司-房地产", "商业公司-其他" };

    private static String checkA(String company) {
        String result = "";
        boolean flag = false;

        String[] commonBlacks = { "公司", "股份", "集团" };
        for (String commonBlack : commonBlacks) {
            if (company.contains(commonBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : a11) {
            if (company.endsWith(s1)){
                result = StrArrayName[0]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : a) {
                if (company.contains(str)){
                    result = StrArrayName[0]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }
        return result;
    }

    private static String checkB(String company) {

        String[] commonBlacks = { "广电网络"};
        for (String commonBlack : commonBlacks) {
            if (company.contains(commonBlack)){
                return "";
            }
        }

        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : b11) {
            if (company.endsWith(s1)){
                result = StrArrayName[1]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : b) {
                if (company.contains(str)){
                    result = StrArrayName[1]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }
        return result;
    }

    private static String checkC(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : c) {
                if (company.contains(str)){
                    result = StrArrayName[2]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }
        return result;
    }

    private static String checkD(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : d) {
                if (company.contains(str)){
                    result = StrArrayName[3]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkE(String company) {

        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : e11) {
            if (company.endsWith(s1)){
                result = StrArrayName[4]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : e) {
                if (company.contains(str)){
                    result = StrArrayName[4]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkF(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : f11) {
            if (company.endsWith(s1)){
                result = StrArrayName[5]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : f) {
                if (company.contains(str)){
                    result = StrArrayName[5]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkG(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : g) {
                if (company.contains(str)){
                    result = StrArrayName[6]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkH(String company) {

        String[] commonBlacks = { "发电", "供电"};
        for (String commonBlack : commonBlacks) {
            if (company.contains(commonBlack)){
                return "";
            }
        }

        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : h) {
                if (company.contains(str)){
                    result = StrArrayName[7]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }
        return result;
    }

    private static String checkI(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : i) {
                if (company.contains(str)){
                    result = StrArrayName[8]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }
        return result;
    }

    private static String checkJ(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : j) {
                if (company.contains(str)){
                    result = StrArrayName[9]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkK(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : k) {
                if (company.contains(str)){
                    result = StrArrayName[10]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkL(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : l) {
                if (company.contains(str)){
                    result = StrArrayName[11]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkM(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : m11) {
            if (company.endsWith(s1)){
                result = StrArrayName[12]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : m) {
                if (company.contains(str)){
                    result = StrArrayName[12]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkN(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : n11) {
            if (company.endsWith(s1)){
                result = StrArrayName[13]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : n) {
                if (company.contains(str)){
                    result = StrArrayName[13]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkO(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : o11) {
            if (company.endsWith(s1)){
                result = StrArrayName[14]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : o) {
                if (company.contains(str)){
                    result = StrArrayName[14]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkP(String company) {
        String result = "";
        boolean flag = false;



        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : p11) {
            if (company.endsWith(s1)){
                result = StrArrayName[15]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : p) {
                if (company.contains(str)){
                    result = StrArrayName[15]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkQ(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : q) {
                if (company.contains(str)){
                    result = StrArrayName[16]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkR(String company) {

        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : r) {
                if (company.contains(str)){
                    result = StrArrayName[17]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkS(String company) {

        String[] commonBlacks = { "移动通信", "联通通信", "联合网络通信", "联合通信", "电信通信", "铁通通信" };
        for (String commonBlack : commonBlacks) {
            if (company.contains(commonBlack)){
                return "";
            }
        }
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : s) {
                if (company.contains(str)){
                    result = StrArrayName[18]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }
        return result;
    }

    private static String checkT(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : t) {
                if (company.contains(str)){
                    result = StrArrayName[19]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkU(String company) {

        String[] commonBlacks = { "广电信息" };
        for (String commonBlack : commonBlacks) {
            if (company.contains(commonBlack)){
                return "";
            }
        }

        boolean flag = false;
        String result = "";
        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : u11) {
            if (company.endsWith(s1)){
                result = StrArrayName[20]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : u) {
                if (company.contains(str)){
                    result = StrArrayName[20]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }


        return result;
    }

    private static String checkV(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : v) {
                if (company.contains(str)){
                    result = StrArrayName[21]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkW(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String str : w11) {
            if (company.contains(str)){
                result = StrArrayName[22]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }


        if (flag && StringUtils.isBlank(result)){
            for (String str : w) {
                if (company.contains(str)){
                    result = StrArrayName[22]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkX(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : x11) {
            if (company.endsWith(s1)){
                result = StrArrayName[23]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : x) {
                if (company.contains(str)){
                    result = StrArrayName[23]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkY(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : y){
                if (company.contains(str)){
                    result = StrArrayName[24]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkZ(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : z){
                if (company.contains(str)){
                    result = StrArrayName[25]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkA1(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : a1){
                if (company.contains(str)){
                    result = StrArrayName[26]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkB1(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : bb11) {
                if (company.endsWith(str)){
                    result = StrArrayName[27]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        if (flag && StringUtils.isBlank(result)){
            for (String str : b1){
                if (company.contains(str)){
                    result = StrArrayName[27]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkC1(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : ce11) {
            if (company.endsWith(s1)){
                result = StrArrayName[28]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : c1){
                if (company.contains(str)){
                    result = StrArrayName[28]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkD1(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String str : d11) {
            if (company.contains(str)){
                result = StrArrayName[29]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }

        if (StringUtils.isBlank(result)){
            if (flag){
                for (String str : d1){
                    if (company.contains(str)){
                        result = StrArrayName[29]+ ConstantBean.RULE_SEPARATOR;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static String checkE1(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        for (String s1 : ee11) {
            if (company.endsWith(s1)){
                result = StrArrayName[30]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
                break;
            }
        }

        if (flag){
            for (String str : e1){
                if (company.contains(str)){
                    result = StrArrayName[30]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkF1(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : f1){
                if (company.contains(str)){
                    result = StrArrayName[31]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkG1(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : g1){
                if (company.contains(str)){
                    result = StrArrayName[32]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkH1(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : h1){
                if (company.contains(str)){
                    result = StrArrayName[33]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkI1(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : i1){
                if (company.contains(str)){
                    result = StrArrayName[34]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkJ1(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : j1){
                if (company.contains(str)){
                    result = StrArrayName[35]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkK1(String company) {
        String result = "";
        boolean flag = false;

        String[] otherBlacks = { "公司", "股份", "集团"};
        for (String otherBlack : otherBlacks) {
            if (company.contains(otherBlack)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : k1){
                if (company.contains(str)){
                    result = StrArrayName[36]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkL1(String company) {
        String result = "";
        for (String str : l1){
            if (company.endsWith(str)){
                result = StrArrayName[37]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        return result;
    }

    /**
     * 执行事业机关规则
     */
    public static String ruleVerification(String company) {

        String resultTag = checkA(company) + checkB(company)+ checkC(company)+ checkD(company) + checkE(company) + checkF(company) + checkG(company) + checkH(company) +
                checkI(company) + checkJ(company) + checkK(company) +  checkL(company) + checkM(company) + checkN(company) + checkO(company) + checkP(company) +
                checkQ(company) + checkR(company) + checkS(company) + checkT(company) + checkU(company) + checkV(company) + checkW(company) + checkX(company) +
                checkY(company) + checkZ(company) + checkA1(company) + checkB1(company) + checkC1(company) + checkD1(company) + checkE1(company) + checkF1(company) +
                checkG1(company) + checkH1(company)  + checkI1(company) + checkJ1(company) + checkK1(company);

        if (StringUtils.isBlank(resultTag)){
            resultTag = checkL1(company);
        }

        if (StringUtils.isNotBlank(resultTag)){
            resultTag = resultTag.substring(0, resultTag.length() - 1);
        }

        return resultTag;
    }

}
