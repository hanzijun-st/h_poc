package com.qianlima.offline.middleground;

import com.alibaba.fastjson.JSON;
import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.NoticeMQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class NiZaiJianService {

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    HashMap<Integer, Area> areaMap = new HashMap<>();

    //地区
    @PostConstruct
    public void init() {
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList("SELECT * FROM phpcms_area");
        for (Map<String, Object> map : maps) {
            Area area = new Area();
            area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
            area.setName(map.get("name").toString());
            area.setParentid(map.get("parentid").toString());
            area.setArrparentid(map.get("arrparentid").toString());
            areaMap.put(Integer.valueOf(map.get("areaid").toString()), area);
        }
    }

    public Map<String, Object> guanWangData(NoticeMQ map) {

        Long contentid = Long.valueOf(map.getContentid());

        log.info("contentid:{} 对应的数据状态不是" , contentid);
        Map<String, Object> map1 = gwJdbcTemplate.queryForMap("SELECT * FROM phpcms_content where contentid=?",contentid);
        String updatetime = map1.get("updatetime").toString(); //跟新时间
        String dateString = secondToDate(Long.valueOf(updatetime),"yyyy-MM-dd hh:mm:ss");

        String xmNumber = map1.get("contentid").toString(); //项目编号

        List<Map<String, Object>> map2 = gwJdbcTemplate.queryForList("SELECT * FROM zdy_xm_all where id=?",contentid);
        if(map2 == null || map2.size() == 0){
            return map1;
        }

        String title = map2.get(0).get("xmmc").toString(); //标题
        String jzjd = map2.get(0).get("jzjd").toString(); //进展阶段
        String xmxz = map2.get(0).get("xmxz").toString(); //项目性质

        List<Map<String, Object>> map3 = gwJdbcTemplate.queryForList("SELECT * FROM zdy_xm_extenddetails where xmid=?",contentid);
        if(map3 == null || map3.size() == 0){
            return map1;
        }

        String yzlx = map3.get(0).get("yzlx").toString(); //业主类型
        if(yzlx.equals("1")){
            yzlx = "商业";
        }else if(yzlx.equals("2")){
            yzlx = "政府";
        }

        String lbA = map3.get(0).get("lbA").toString(); //项目类别
        StringBuilder lbAStr = new StringBuilder(); //项目类别
        String[] split1 = lbA.split(",");
        for (String s : split1) {
            Map<String, Object> jzjdmapping = gwJdbcTemplate.queryForMap("SELECT * FROM zdy_xm_typemapping where xmid=?",Integer.valueOf(s));
            lbAStr.append(jzjdmapping.get("xmname")+",");
        }
        lbA = lbAStr.toString().substring(0,lbAStr.toString().length() - 1);

        String lbB = map3.get(0).get("lbB").toString(); //项目子类别
        StringBuilder lbBStr = new StringBuilder(); //项目类别
        String[] split2 = lbB.split(",");
        for (String s : split2) {
            Map<String, Object> jzjdmapping = gwJdbcTemplate.queryForMap("SELECT * FROM zdy_xm_typemapping where xmid=?",Integer.valueOf(s));
            lbBStr.append(jzjdmapping.get("xmname")+",");
        }
        lbB = lbBStr.toString().substring(0,lbBStr.toString().length() - 1);

        String tze = map2.get(0).get("tze").toString(); //项目投资
        if(StringUtils.isBlank(tze) || tze.equals("0")){
            tze = map3.get(0).get("gsje").toString();
            if(StringUtils.isBlank(tze)){
                tze = "不确定";
            }
        }
        String kgrq = map3.get(0).get("kgrq").toString(); //开工时间
        String jgrq = map3.get(0).get("jgrq").toString(); //竣工时间
        String jzmj = map3.get(0).get("jzmj").toString(); //建筑面积
        String zdmj = map3.get(0).get("zdmj").toString(); //占地面积
        String jzcs = map3.get(0).get("jzcs").toString().equals("0")?"不确定":map3.get(0).get("jzcs").toString(); //建筑物层数

        String gjg = "";//钢结构
        if(map3.get(0).get("gjg").toString().equals("0")){
            gjg = "否";
        }else if(map3.get(0).get("gjg").toString().equals("1")){
            gjg = "是";
        }else{
            gjg = "不确定";
        }
        String zxqk = ""; //装修情况
        if(map3.get(0).get("zxqk").toString().equals("0")){
            zxqk = "不装修"; //钢结构
        }else if(map3.get(0).get("zxqk").toString().equals("1")){
            zxqk = "简装修"; //钢结构
        }else if(map3.get(0).get("zxqk").toString().equals("2")){
            zxqk = "精装修"; //钢结构
        }
        String zxbz = map3.get(0).get("zxbz").toString(); //装修标准
        if(StringUtils.isBlank(zxbz)){
            zxbz = "不确定";
        }
        String wqys = map3.get(0).get("wqys").toString(); //外墙预算
        if(StringUtils.isBlank(wqys)){
            wqys = "不确定";
        }
        String xmdz = map3.get(0).get("xmdz").toString(); //项目地址

        String content = map2.get(0).get("content").toString();//项目概括


        List<Map<String, Object>> map4 = gwJdbcTemplate.queryForList("SELECT * FROM zdy_xm_lxrmore where xmid=?",contentid);

        List<Map> yezhuList = new ArrayList<>();
        List<Map> shejiList = new ArrayList<>();
        List<Map> shigongList = new ArrayList<>();
        for (Map<String, Object> object : map4) {
            if(Integer.valueOf(object.get("type").toString()) == 1){
                Map<String,Object> yezhu = new HashMap<>();
                yezhu.put("单位名称",object.get("dwmc")); //单位名称
                yezhu.put("地址",object.get("addr")); //地址
                yezhu.put("联系人",object.get("lxr")); //联系人
                yezhu.put("状态",object.get("isJob")); //状态
                yezhu.put("职位",object.get("Zhiwei")); //职位
                yezhu.put("性别",object.get("Sex")); //性别
                yezhu.put("手机",object.get("mobile")); //手机
                yezhu.put("电话",object.get("phone")); //电话
                yezhu.put("emial",object.get("email")); //emial
                yezhu.put("传真",object.get("fax")); //传真
                yezhu.put("备注",object.get("beizhu")); //备注
                yezhu.put("类型",object.get("type")); //类型
                yezhuList.add(yezhu);

                bdJdbcTemplate.update("INSERT INTO `xiangmu_data_sheet2`(`taskid`,`xmNumber`, `title`, `dwmc`, `dept`, `lxr`, `phone`, `mobile`, `type`) VALUES (?,?,?,?,?,?,?,?,?)",
                        map.getTaskId(),contentid,title,object.get("dwmc"),object.get("Zhiwei"),object.get("lxr"),object.get("phone"),object.get("mobile"),"业主");

            }else if(Integer.valueOf(object.get("type").toString()) == 2){
                Map<String,Object> sheji = new HashMap<>();
                sheji.put("单位名称",object.get("dwmc")); //单位名称
                sheji.put("地址",object.get("addr")); //地址
                sheji.put("联系人",object.get("lxr")); //联系人
                sheji.put("状态",object.get("isJob")); //状态
                sheji.put("职位",object.get("Zhiwei")); //职位
                sheji.put("性别",object.get("Sex")); //性别
                sheji.put("手机",object.get("mobile")); //手机
                sheji.put("电话",object.get("phone")); //电话
                sheji.put("emial",object.get("email")); //emial
                sheji.put("传真",object.get("fax")); //传真
                sheji.put("备注",object.get("beizhu")); //备注
                sheji.put("类型",object.get("type")); //类型
                shejiList.add(sheji);
                bdJdbcTemplate.update("INSERT INTO `xiangmu_data_sheet2`(`taskid`,`xmNumber`, `title`, `dwmc`, `dept`, `lxr`, `phone`, `mobile`, `type`) VALUES (?,?,?,?,?,?,?,?,?)",
                        map.getTaskId(),contentid,title,object.get("dwmc"),object.get("Zhiwei"),object.get("lxr"),object.get("phone"),object.get("mobile"),"设计");
            }else if(Integer.valueOf(object.get("type").toString()) == 3){
                Map<String,Object> shigong = new HashMap<>();
                shigong.put("单位名称",object.get("dwmc")); //单位名称
                shigong.put("地址",object.get("addr")); //地址
                shigong.put("联系人",object.get("lxr")); //联系人
                shigong.put("状态",object.get("isJob")); //状态
                shigong.put("职位",object.get("Zhiwei")); //职位
                shigong.put("性别",object.get("Sex")); //性别
                shigong.put("手机",object.get("mobile")); //手机
                shigong.put("电话",object.get("phone")); //电话
                shigong.put("emial",object.get("email")); //emial
                shigong.put("传真",object.get("fax")); //传真
                shigong.put("备注",object.get("beizhu")); //备注
                shigong.put("类型",object.get("type")); //类型
                shigongList.add(shigong);
                bdJdbcTemplate.update("INSERT INTO `xiangmu_data_sheet2`(`taskid`,`xmNumber`, `title`, `dwmc`, `dept`, `lxr`, `phone`, `mobile`, `type`) VALUES (?,?,?,?,?,?,?,?,?)",
                        map.getTaskId(),contentid,title,object.get("dwmc"),object.get("Zhiwei"),object.get("lxr"),object.get("phone"),object.get("mobile"),"施工");
            }
        }


        List<Area> areaList = new ArrayList<>();
        String provinceStr = null; //旧省
        String cityStr = null; //旧市
        String countryStr = null; //旧县
        if (null != map.getAreaid()) {
            if (!areaMap.isEmpty()) {
                Area area = areaMap.get(Integer.valueOf(map.getAreaid()));
                if (area != null) {
                    int province = -1;
                    int city = -1;
                    int country = -1;
                    int areaid = area.getAreaid();
                    String arrparentid = area.getArrparentid().trim();
                    String[] temp = arrparentid.split(",");
                    if (temp.length == 1) {
                        province = areaid;
                        provinceStr = areaMap.get(province).getName();
                    } else if (temp.length == 2) {
                        province = Integer.valueOf(temp[1]);
                        city = areaid;
                        provinceStr = areaMap.get(province).getName();
                        cityStr = areaMap.get(city).getName();
                    } else if (temp.length == 3) {
                        province = Integer.valueOf(temp[1]);
                        city = Integer.valueOf(temp[2]);
                        country = areaid;

                        provinceStr =areaMap.get(province).getName();
                        cityStr =areaMap.get(city).getName();
                        countryStr =areaMap.get(country).getName();
                    }
                }
            }
        }
        for (Area area : areaList) {
            areaMap.put(area.getAreaid(), area);
        }
        log.info("处理到:{}",atomicInteger.incrementAndGet());

        String[] ggc = {"住宅","住建","匝道","园区","渔业","渔港","物业","铁路","隧道","水域","桥梁","码头","矿山","口岸","卡口","酒店","监狱","机场","会展","航道","海域","海洋","海湾","海事","海关","海岛","管廊","公寓","工地","高铁","港口","法院","堆场","地铁","道口","场站","场馆","综合体","自贸区","招待所","展览馆","养老院","写字楼","洗煤厂","危固废","停车楼","收费站","适用房","省国道","洽谈室","廉租房","空管局","看守所","拘留所","戒毒所","检察院","航站楼","福利院","服务区","度假村","出入口","车管所","产业园","采集站","殡仪馆","保税区","保护区","安置区","安置房","综治中心","指挥中心","指挥大厅","物流园区","调度中心","数据中心","市民中心","湿地公园","美丽海湾","矛调中心","露天煤矿","救援基地","精神病院","结算中心","航空公司","行政中心","海洋公园","国家公园","轨道交通","购物中心","耕地保护","高铁周界","高速公路","航空货运站","港口物流公司","视频会议室建设","视频会议室技改","视频会议室改造","综合交通枢纽航空物流园","TOCC交通运行监测调度中心"};

        //正常输出关键词
//        String keyword = map.getKeyword();
        //多个关键词 追加
        content = title + "&" + content;
        content = content.toUpperCase();
        String keyword = "";
        for (String gc : ggc) {
            if (content.contains(gc) ) {
                keyword += (gc + "、");
            }
        }
        if (StringUtils.isNotBlank(keyword)) {
            keyword = keyword.substring(0, keyword.length() - 1);
        }
        String code = "";
        String keyword_term = "";

        bdJdbcTemplate.update("INSERT INTO xiangmu_data_1(`contentid`,`title`, `updatetime`, `provinceStr`, `cityStr`, `countryStr`, `xmNumber`, `jzjd`, `xmxz`, `yzlx`, `lbA`, `lbB`, `tze`, `kgrq`, `jgrq`, `jzmj`, `zdmj`, `jzcs`, `gjg`, `zxqk`, `zxbz`, `wqys`, `xmdz`, `content`, `yezhuList`, `shejiList`, `shigongList`,`taskid`,`keyword`, `code`, `keyword_term`) VALUES (?,?,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)",
                contentid, title, dateString, provinceStr, cityStr, countryStr, xmNumber, jzjd, xmxz, yzlx, lbA, lbB, tze, kgrq, jgrq, jzmj, zdmj, jzcs, gjg, zxqk, zxbz, wqys, xmdz, content, JSON.toJSONString(yezhuList), JSON.toJSONString(shejiList), JSON.toJSONString(shigongList), map.getTaskId(), keyword, code, keyword_term);
        return map1;
    }

    private String secondToDate(long second,String patten) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(second * 1000);//转换为毫秒
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(patten);
        String dateString = format.format(date);
        return dateString;
    }

}
