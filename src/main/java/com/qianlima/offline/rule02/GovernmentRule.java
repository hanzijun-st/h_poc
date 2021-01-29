package com.qianlima.offline.rule02;

import com.qianlima.offline.bean.ConstantBean;
import org.apache.commons.lang3.StringUtils;

public class GovernmentRule {

    private static String[] a = { "军委", "国防" ,"解放军" ,"武装部" ,"军人" ,"军粮" ,"部队" ,"炮兵" ,"军用" ,"战区" ,"战备" ,"预备役" ,"武装" ,"士兵" ,"勤务" ,"民兵" ,"陆军" ,"空军" ,"军事" ,"军官" ,"军供" ,"军工" ,"参谋" ,"步兵" ,"兵器" ,"征兵" ,"军民融合" ,"作战指挥"};
    private static String[] b = { "公安" ,"派出所" ,"边防" ,"边检" ,"消防" ,"交警" ,"武警" ,"警察" ,"管教所" ,"移民局" ,"戒毒" ,"边境" ,"安保指挥" ,"安保服务" ,"治安" ,"移民" ,"车辆管理" ,"非法" ,"超限" ,"超载" ,"拘留" ,"看守所" ,"交通管理" ,"交管大队" ,"禁毒" ,"执勤" ,"巡逻" ,"巡警" ,"刑侦" ,"刑警" ,"邪教" ,"协警" ,"违法" ,"网警" ,"特警" ,"事故" ,"身份证" ,"取证" ,"联防" ,"纠纷" ,"警用" ,"警务" ,"海警" ,"公共安全" ,"辅警" ,"反恐" ,"办案" ,"安全保卫" ,"交通大队" ,"交通执法" ,"流动人口"};
    private static String[] c = { "法院" ,"检察院" ,"司法" ,"监狱" ,"监所" ,"法律" ,"仲裁" ,"检察分院" ,"法制" ,"检察机关" ,"律师管理" ,"劳动教养" ,"劳动改造" ,"律师事务" ,"检务" ,"诉讼服务"};
    private static String[] d = { "政协", "政治", "人大", "常委", "常务委员会", "社会主义", "维稳", "人民代表", "社会建设工作", "党工委", "政法" ,"党委" ,"巡视" ,"政策" ,"党群" ,"党政" ,"综治" ,"综合治理" ,"巡察" ,"社会治理" ,"舆情" ,"舆论" ,"协调" ,"社会管理" ,"党建服务" ,"社会稳定" ,"督查办公室" };
    private static String[] e = { "纪律检查" ,"纪委" ,"纪律委员会" ,"纪检" ,"纪律监察" ,"纪律监查" ,"纪律检察","监察委员会" ,"监委", "监察局"};
    private static String[] f = { "宣传部" ,"对外宣传" ,"宣传办公室" ,"宣传工作" ,"中宣部"};
    private static String[] g = { "组织部" ,"组织工作" ,"组织委员会" ,"老干部" ,"老龄工作委员会"};
    private static String[] h = { "领导工作", "街道办事处" ,"信访局" ,"行政审批" ,"机关事务" ,"机要局" ,"保密委员会" ,"档案" ,"国资委"  ,"政务服务" ,"综合行政" ,"人民政府" ,"机关服务" ,"行政公署" ,"办事处" ,"综合事务" ,"行政服务" ,"老干部工作" ,"国有资产" ,"区管理" ,"区管委" ,"政府办公室" ,"管委会" ,"管理委会员" ,"政府采购中心" ,"政务" ,"市委办公" ,"县委办公室" ,"区委办公室" ,"市委员会" ,"县委员会" ,"区委员会" ,"行政管理" ,"行政事务" ,"民意调查" ,"省委办" ,"省委员会" ,"区工作" ,"社区服务" ,"社会服务" ,"区级机关" ,"市政府" ,"省政府" ,"县政府" ,"乡政府" ,"镇政府" ,"资产与实验室" ,"园管理" ,"机关事务" ,"接待办" ,"省机关" ,"市机关" ,"县机关" ,"区机关" ,"信访工作" ,"机关工作" ,"地方事业" ,"政府服务" ,"镇委员会" ,"群众工作" ,"机构编制" ,"区政府" ,"密码管理" ,"廉政" ,"街道工作" ,"信访办公室" ,"社区发展" ,"社区事务" ,"区办公室" ,"公有资产"};
    private static String[] i = { "海关" ,"检验检疫" ,"口岸局" ,"口岸管理" ,"海防" ,"口岸办" ,"进出口管理" ,"口岸服务", "缉私", "口岸业务", "走私"};
    private static String[] j = { "外交" ,"外事" ,"对外" ,"外国专家" ,"外文局" ,"侨务" ,"港澳工作" ,"台湾工作"};
    private static String[] k = { "重大项目", "脱贫攻坚", "联扶工作", "促进中心", "社发局", "促进委员会", "发改委" ,"发改局" ,"发展和改革" ,"扶贫" ,"统战" ,"统计" ,"粮食局" ,"中小企业" ,"统一战线" ,"社会发展" ,"贸易促进" ,"商务局"  ,"招商局" ,"发展与改革" ,"发展改革" ,"促进局" ,"商务委员会" ,"改革" ,"招商服务" ,"帮扶" ,"区域发展" ,"商务厅" ,"商务部" ,"商务中心" ,"市场发展" ,"促进协会" ,"促进会" ,"促进委" ,"投资促进" ,"生产力促进" ,"投资服务" ,"创业服务" ,"地方调查" ,"合作交流" ,"粮食管理"};
    private static String[] l = { "科技厅", "科学技术厅", "中科院","研究设计", "中国科学院" ,"科技创新" ,"航天" ,"核技术" ,"科协" ,"科技局" ,"技术研究院" ,"科学研究院" ,"技术研究所" ,"科学研究所" ,"极地" ,"卫星" ,"发动机" ,"物理" ,"科技信息" ,"自然科学" ,"科技发展" ,"科学技术局" ,"科学技术协会" ,"科学技术委员会" ,"社会科学院" ,"农业科学院" ,"医学科学院" ,"航发" ,"研究所" ,"研究院" ,"研究中心" ,"科学院" ,"科学技术工作" ,"实验室" ,"创新发展"};
    private static String[] m = { "民主团体", "残联", "妇联", "基督教", "佛教", "民族事务" ,"宗教" ,"民间组织" ,"组织机构" ,"联合会" ,"工会" ,"社会团体" ,"社会组织" ,"社团" ,"共青团" ,"妇联机关"};
    private static String[] n = { "招才引智", "人事局", "高技能人才", "人事办公室", "医疗保障", "人力资源" ,"人社" ,"就业" ,"劳动服务" ,"劳动保障" ,"社会保险" ,"医疗保险" ,"社会保障" ,"人才服务" ,"人力社保" ,"养老保险" ,"退休干部" ,"职业技能鉴定" ,"社保" ,"人才交流" ,"人才发展" ,"人才工作" ,"劳动监察"};
    private static String[] o = { "会计", "财政" ,"审计" ,"会计管理" ,"会计事务" ,"支付中心" ,"注册会计"};
    private static String[] p = { "税务" ,"国税" ,"地税" ,"非税" ,"治税" ,"财税" ,"税费" ,"税源", "税收"};
    private static String[] q = { "慈善", "社会事务", "社会事业", "民政部" ,"民政厅" ,"民政局" ,"居民委员会" ,"社会救助" ,"彩票" ,"福利" ,"救助" ,"便民" ,"居委会" ,"殡仪馆" ,"陵园" ,"敬老院" ,"敬老服务" ,"养老服务" ,"婚姻" ,"业主委员会" ,"民生服务" ,"捐助" ,"殡葬" ,"民政事务" ,"社会工作" ,"最低生活保障"};
    private static String[] r = { "城市服务管理", "公共治理", "城市治理", "城市管理" ,"市政局" ,"市政管理" ,"市民服务" ,"城市综合管理" ,"公用事业" ,"公共资源" ,"城管" ,"照明" ,"路灯" ,"公共服务" ,"市政工程" ,"公园" ,"园林" ,"为民服务" ,"城市景观" ,"公共采购" ,"综合执法" ,"综合服务" ,"区市政" ,"动物园" ,"植物园" ,"城市指挥" ,"市长热线" ,"市政设施" ,"行政执法" ,"公共事业" ,"公用设施" ,"市容管理" ,"城市运行" ,"城市发展" ,"市政维护" ,"公共事务" ,"城市执法" ,"市政设计" ,"市执法" ,"公共设施" ,"公用局"};
    private static String[] s = { "勘测院", "海洋监测", "资源保护", "自然资源" ,"国土资源" ,"地质" ,"土地" ,"测绘" ,"地理" ,"国土" ,"草原" ,"湿地" ,"森林资源" ,"海洋局" ,"勘测规划" ,"海监" ,"规划勘测" ,"勘察院" ,"渔政" ,"海洋发展"};
    private static String[] t = { "生态环境" ,"环境保护" ,"自然保护" ,"环保" ,"环卫" ,"环境卫生" ,"环境监测" ,"环境监察" ,"污染" ,"生活垃圾" ,"废弃物" ,"环境整治" ,"环境综合" ,"废物处置" ,"环境" ,"生态保护" ,"保护局" ,"绿化" ,"固体废物" ,"垃圾" ,"渣土" ,"绿色发展"};
    private static String[] u = { "气象" ,"天气" ,"气候" ,"雨量" ,"雨水" ,"人工降雨", "海洋预报"};
    private static String[] v = { "电教教仪", "健身", "体委", "体育委", "教学研究", "教育和体育", "教育体育", "体育局", "体育指导", "体育事业", "体育部", "体育厅", "体育中心", "体育服务", "教体", "竞技", "比赛", "训练", "竞赛", "电竞", "乒乓球", "篮球", "足球", "运动管理", "水上运动", "棋牌运动", "电教仪器", "教育保障", "教育馆", "教仪中心", "教育发展", "教育资产", "教育装备", "教育教学", "教育部" ,"教育厅" ,"教育局" ,"教委" ,"招生" ,"考试" ,"勤工俭学" ,"文教" ,"教师发展" ,"教育委员会" ,"教育技术" ,"教育矫治" ,"教育事务" ,"教育工作" ,"教育促进" ,"教育办公室" ,"宣教局" ,"教育研究" ,"教育服务" ,"教育指导"};
    private static String[] w = { "卫健局", "卫健委", "健康教育所", "就医管理", "医学中心", "卫计局", "保健委员会", "卫生事务", "卫生部" ,"卫生厅" ,"卫生局" ,"计划生育" ,"卫生健康" ,"医药管理" ,"卫计委" ,"卫生监督" ,"红十字会" ,"计生委" ,"卫生计生" ,"卫生和计划" ,"医局" ,"医疗机构" ,"干细胞捐献" ,"卫生事业" ,"医药产业发展" ,"医药协会" ,"医药卫生" ,"爱国卫生"};
    private static String[] x = { "期货", "经贸发展", "股票", "经济合作", "反洗钱", "经信局", "社会信用", "证券业", "金融" ,"外汇" ,"经济委员会" ,"经济局" ,"经济服务" ,"银监局" ,"货币" ,"经济管理" ,"经济发展" ,"人民币" ,"保监局" ,"货币" ,"银监会" ,"保监会" ,"保险监督" ,"经济运行" ,"银行业监督管理" ,"基金会" ,"信用中心" ,"银监分局" ,"经济贸易"};
    private static String[] y = { "电业局", "能源" ,"燃气管理" ,"供热" ,"矿产资源" ,"钻井" ,"地矿" ,"石油管理" ,"矿山" ,"矿产" ,"煤矿" ,"电站" ,"石油化工" ,"供电" ,"电力局" ,"油田" ,"节能"};
    private static String[] z = { "水利" ,"水务" ,"灌区管理" ,"水土" ,"水库管理" ,"污水" ,"水文" ,"排水" ,"水政" ,"湖泊" ,"水源" ,"水资源" ,"河道" ,"河堤" ,"河长" ,"河管" ,"湖管" ,"供水" ,"节约用水" ,"南水北调" ,"灌溉" ,"水电" ,"节水" ,"水质" ,"河流" ,"引水管理" ,"水闸" ,"船闸" ,"围垦" ,"河务" ,"河涌管理" ,"流域管理"};
    private static String[] a1 = { "轨道交通", "交运局", "综合交通", "渔港渔船", "交通通信", "交通科技", "物流局", "交通运输" ,"交通局" ,"海事" ,"公路" ,"运输管理" ,"路政" ,"港航" ,"铁路" ,"铁道" ,"火车" ,"客运" ,"交通安全" ,"路桥" ,"民用航空" ,"车务段" ,"机务段" ,"航道" ,"邮政" ,"工务段" ,"机场" ,"港口" ,"高速管理" ,"电务段" ,"交通厅" ,"航务" ,"交通委" ,"水运" ,"道路" ,"物流中心" ,"飞行管理" ,"汽车站" ,"交通服务" ,"交通发展" ,"船舶" ,"船级" ,"物流发展" ,"隧道管理" ,"车站" ,"公共交通" ,"港务" ,"运输事业"};
    private static String[] b1 = { "信息服务", "数据服务", "政数局", "电子政务", "工业技术", "通信局", "计算中心", "网络信息", "数据统筹", "工业和信息化" ,"信息化" ,"大数据" ,"互联网" ,"数据管理" ,"数据资源" ,"无线电" ,"工信" ,"网络管理" ,"网格管理" ,"网格化" ,"省信息中心" ,"市信息中心" ,"县信息中心" ,"烟草" ,"网络安全" ,"通讯" ,"计算机" ,"国家信息" ,"化学工业管理" ,"信息中心" ,"信息通信" ,"数字化" ,"数字城市" ,"通信管理" ,"网络中心" ,"数据中心" ,"信息局" ,"电子商务" ,"信息技术" ,"煤炭工业" ,"煤监局" ,"信息管理" ,"矿务局" ,"煤炭管理" ,"电子信息" ,"信息委员会"};
    private static String[] c1 = { "农谷管理", "林业" ,"农牧局" ,"畜牧" ,"水产" ,"农业" ,"农村" ,"村民" ,"农林" ,"农牧业" ,"渔业" ,"养殖" ,"村委会" ,"饲料" ,"农作物" ,"农牧" ,"种子" ,"林管" ,"农机" ,"农水" ,"村办公室" ,"农垦" ,"土壤肥料" ,"农场" ,"植保植检" ,"牧业" ,"种植业" ,"果业" ,"茶业管理" ,"林木种苗" ,"畜禽繁育" ,"村委" ,"病虫害"};
    private static String[] d1 = { "广播影视", "文化宫", "广播电影", "文化办公室", "博览事务", "展览事务", "青少年文化", "版权", "文化和旅游" ,"文化局" ,"旅游局" ,"广播艺术" ,"旅游事业" ,"广播电视" ,"文化旅游" ,"党史" ,"知识产权" ,"文化馆" ,"电视台" ,"新闻" ,"广电局" ,"纪念馆" ,"文化遗产" ,"影视局" ,"博物馆" ,"文广新" ,"文物" ,"旅游" ,"文体" ,"剧院" ,"歌舞" ,"剧团" ,"文工团" ,"少年宫" ,"广播电台" ,"专利" ,"艺术团" ,"博物院" ,"文化厅" ,"文化部" ,"考古" ,"遗址" ,"精神文明" ,"门票" ,"活动中心" ,"文化艺术" ,"公共文化" ,"电视局" ,"综合文化" ,"文化服务" ,"有线电视" ,"文化广播" ,"文化站" ,"文化馆" ,"艺术中心" ,"文化委员会" ,"广电总局" ,"旅游发展" ,"旅游产业" ,"地方史志" ,"景区保护" ,"出版发行" ,"编纂委员会" ,"文化发展" ,"文化产业发展" ,"文旅广新局" ,"史志办" ,"作家协会" ,"文化工作"};
    private static String[] e1 = { "城乡综合管理", "建设和管理", "三改一拆", "建设发展局", "公共建设", "住房和城乡建设" ,"住房" ,"房产" ,"不动产" ,"房地产" ,"房屋" ,"拆迁" ,"城乡规划" ,"建设局" ,"住建" ,"城乡建设" ,"房管局" ,"公房管理" ,"公共建筑" ,"城市建设" ,"搬迁" ,"城乡管理" ,"城建" ,"产权" ,"建筑" ,"建设管理" ,"征地" ,"城乡工作" ,"规划局" ,"规划委员会" ,"规划管理" ,"建设服务中心" ,"公租房" ,"公共租赁房" ,"工务局" ,"城市规划" ,"棚改指挥" ,"城乡统筹" ,"征拆指挥" ,"工程管理局" ,"城镇管理" ,"城乡发展" ,"公积金管理"};
    private static String[] f1 = { "食品化妆品监督", "市场监督" ,"市场监管" ,"工商局" ,"技术监督" ,"物价" ,"工商行政" ,"质量监督" ,"食品药品" ,"药品监督" ,"质量检验" ,"检验检测" ,"检验所" ,"检验局" ,"粮食质量" ,"标准化" ,"消费" ,"质量检测" ,"质监局" ,"计量监督" ,"检测所" ,"测试所" ,"监督检验" ,"设备检测" ,"计量" ,"价格" ,"市场管理" ,"质量监测" ,"监督管理" ,"质量安全" ,"投诉举报" ,"质量控制" ,"质量管理" ,"食品监督" ,"食品监测" ,"广告监测" ,"影视监测" ,"药监局" ,"渔港监督" ,"检验监测" ,"产品质量"};
    private static String[] g1 = { "搜救", "应急" ,"防空" ,"安全监察" ,"安全生产" ,"地震" ,"防汛" ,"救灾" ,"防洪" ,"储备" ,"安监局" ,"防火" ,"民防" ,"安全监督" ,"紧急救援" ,"燃气安全" ,"排洪" ,"排涝" ,"生产安全" ,"粮库" ,"人防" ,"防震" ,"减灾" ,"预警" ,"急救指挥" ,"灾害" ,"安监站" ,"安全管理" ,"安全监管" ,"防雷中心" ,"抗震办公室"};
    private static String[] h1 = { "部", "团", "委", "厅", "局", "处", "党", "政", "会", "所", "办", "科", "室", "站", "队", "署", "院", "社", "台", "宫", "园", "馆", "组", "司", "庭", "关", "狱", "政府", "基地", "机构", "单位", "组织", "中心"};
    private static String[] x1 = { "银行业监督管理", "保险监督", "证监会", "证券监督" };
    private static String[] a11 = {"车务段", "机务段", "车辆段", "电务段", "工务段", "车辆管理段", "公路管理段"};
    private static String[] g11 = {"粮库", "储备库"};
    private static String[] c11 = {"林场"};
    private static String[] h11 = {"政府", "办公室", "局", "厅", "处", "委员会", "中心", "部", "单位", "委", "机关"};

    private static String[] StrArrayName = { "政府机构-国防", "政府机构-公安", "政府机构-检法司", "政府机构-政法委", "政府机构-纪委", "政府机构-宣传", "政府机构-组织", "政府机构-地方政务", "政府机构-海关", "政府机构-外交", "政府机构-发展和改革", "政府机构-科学技术", "政府机构-民族事务", "政府机构-人力资源和社会保障", "政府机构-财政", "政府机构-税务", "政府机构-民政", "政府机构-市政", "政府机构-自然资源", "政府机构-生态环境", "政府机构-气象", "政府机构-教育", "政府机构-医疗", "政府机构-金融", "政府机构-能源", "政府机构-水利水电", "政府机构-交通运输", "政府机构-工业和信息化", "政府机构-农业农村", "政府机构-文化和旅游", "政府机构-住房和城乡建设", "政府机构-市场监督", "政府机构-应急管理" };


    private static String checkA(String company) {
        String result = "";
        boolean flag = false;
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
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

        String newCompany = company;
        String[] commonBlacks = { "公安县", "派出所辖区"};
        for (String commonBlack : commonBlacks) {
            if (company.contains(commonBlack)){
                newCompany = newCompany.replace(commonBlack, "");
            }
        }

        String result = "";
        boolean flag = false;
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : b) {
                if (newCompany.contains(str)){
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
        for (String common : h1) {
            if (company.contains(common)){
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
        for (String common : h1) {
            if (company.contains(common)){
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
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
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
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
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
        for (String common : h1) {
            if (company.contains(common)){
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
        String result = "";
        boolean flag = false;
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
                break;
            }
        }

        for (String str : h11) {
            if (company.endsWith(str)){
                result = StrArrayName[7]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }
        if (StringUtils.isBlank(result)){
            if (flag){
                for (String str : h) {
                    if (company.contains(str)){
                        result = StrArrayName[7]+ ConstantBean.RULE_SEPARATOR;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static String checkI(String company) {

        String newCompany = company;
        String[] commonBlacks = { "山海关"};
        for (String commonBlack : commonBlacks) {
            if (company.contains(commonBlack)){
                newCompany = newCompany.replace(commonBlack, "");
            }
        }

        String result = "";
        boolean flag = false;
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : i) {
                if (newCompany.contains(str)){
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
        for (String common : h1) {
            if (company.contains(common)){
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
        for (String common : h1) {
            if (company.contains(common)){
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
        for (String common : h1) {
            if (company.contains(common)){
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
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
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
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
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
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
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
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
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
        for (String common : h1) {
            if (company.contains(common)){
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
        for (String common : h1) {
            if (company.contains(common)){
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
        String result = "";
        boolean flag = false;
        for (String common : h1) {
            if (company.contains(common)){
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
        for (String common : h1) {
            if (company.contains(common)){
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
        String result = "";
        boolean flag = false;
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
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
        for (String common : h1) {
            if (company.contains(common)){
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
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
                break;
            }
        }

        if (flag){
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

        for (String str : x1) {
            if (company.contains(str)){
                result = StrArrayName[23]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }

        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
                break;
            }
        }

        if (flag && StringUtils.isBlank(result)){
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
        for (String common : h1) {
            if (company.contains(common)){
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
        for (String common : h1) {
            if (company.contains(common)){
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
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
                break;
            }
        }

        for (String str : a11){
            if (company.contains(str)){
                result = StrArrayName[26]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
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
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
                break;
            }
        }

        if (company.endsWith("工业园")){
            result = StrArrayName[27]+ ConstantBean.RULE_SEPARATOR;
        }

        if (StringUtils.isBlank(result)){
            if (flag){
                for (String str : b1){
                    if (company.contains(str)){
                        result = StrArrayName[27]+ ConstantBean.RULE_SEPARATOR;
                        break;
                    }
                }
            }
        }


        return result;
    }

    private static String checkC1(String company) {
        String result = "";
        boolean flag = false;
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
                break;
            }
        }

        for (String str : c11){
            if (company.endsWith(str)){
                result = StrArrayName[28]+ ConstantBean.RULE_SEPARATOR;
                break;
            }
        }

        if (flag && StringUtils.isBlank(result)){
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
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
                break;
            }
        }

        if (flag){
            for (String str : d1){
                if (company.contains(str)){
                    result = StrArrayName[29]+ ConstantBean.RULE_SEPARATOR;
                    break;
                }
            }
        }

        return result;
    }

    private static String checkE1(String company) {
        String result = "";
        boolean flag = false;
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
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
        for (String common : h1) {
            if (company.contains(common)){
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
        for (String common : h1) {
            if (company.contains(common)){
                flag = true;
                break;
            }
        }
        for (String str : g11){
            if (company.contains(str)){
                result = StrArrayName[32]+ ConstantBean.RULE_SEPARATOR;
                flag = false;
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

    /**
     * 执行事业机关规则
     */
    public static String ruleVerification(String company) {

        String resultTag = checkA(company) + checkB(company)+ checkC(company)+ checkD(company) + checkE(company) + checkF(company) + checkG(company) +
                checkH(company) + checkI(company) + checkJ(company) + checkK(company) +  checkL(company) + checkM(company) + checkN(company) +
                checkO(company) + checkP(company) + checkQ(company) + checkR(company) + checkS(company) + checkT(company) + checkU(company) +
                checkV(company) + checkW(company) + checkX(company) + checkY(company) + checkZ(company) + checkA1(company) + checkB1(company) +
                checkC1(company) + checkD1(company) + checkE1(company) + checkF1(company) + checkG1(company);

        if (StringUtils.isNotBlank(resultTag)){
            resultTag = resultTag.substring(0, resultTag.length() - 1);
        }

        return resultTag;
    }
}
