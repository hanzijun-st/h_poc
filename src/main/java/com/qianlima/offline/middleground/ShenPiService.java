package com.qianlima.offline.middleground;

import com.qianlima.approval.bean.ExtractInvestAmount;
import com.qianlima.approval.bean.ExtractNature;
import com.qianlima.approval.bean.ExtractOwner;
import com.qianlima.approval.extractor.InvestAmountExtractor;
import com.qianlima.approval.extractor.NatureExtractor;
import com.qianlima.approval.extractor.PropertyOwnerExtractor;
import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.bean.ItemInfo;
import com.qianlima.offline.bean.NoticeMQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ShenPiService {

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    NewZhongTaiService zhongTaiService;

    HashMap<Integer, Area> areaMap = new HashMap<>();


    String[] blacks = { "监理", "房屋租赁", "配套设备", "云计算", "云存储", "公务车", "云平台", "软件", "信息系统", "硬件", "设备", "OA", "ICT", "餐饮承包", "食堂承包", "注册平台", "材料采购", "空调采购", "技术平台", "办公系统", "安全系统", "管理系统", "数据库", "系统集成", "网管支撑能力", "云建", "网建", "装备", "网站", "印刷", "印制", "打印", "物料", "信息化系统", "网格化", "网综平台", "建站系统", "数据分析平台", "视频", "音频", "终端", "交通系统", "监控系统", "配送服务", "网络安全", "开发服务", "集成服务", "商业保险", "会议系统", "净水器", "网络备份", "数据服务", "委托管理", "反应质谱", "抑尘系统", "爱心奶", "计算机", "耗材", "保洁", "保养", "桌面云", "尸体", "人员培训", "测试剂盒", "警车", "训练器材", "防水卷材", "密封垫", "自粘胶膜", "运输服务", "碎石采购", "沥青采购", "体检", "看管服务", "出租", "物业管理", "物业服务", "房屋招租", "房屋续租", "维修费", "参数测量", "服务类第四批集中", "水深测量", "智慧校园采购", "平台运维", "暗室采购", "智能化系统", "微架构子系统", "多物理谱仪", "政务外网", "高水平专业群建设", "无线网络覆盖", "网络运维", "系统运行维护", "物资类公开招标", "井盖采购", "满意度回访平台", "开关驱动源", "系统", "网络维护", "运行维护", "网络维保", "固废危废核查", "4G流量池", "水深测量服务", "随机仿真验证", "机房托管", "运维检修", "残联研究课题", "LED屏采购", "卫星天线购置", "树木移植", "砍伐死亡树木", "智能计算平台", "质量监测服务", "水泵大修", "机房维修", "供暖入网服务", "正式用电接入", "门面房购置", "审计", "扫描电镜", "电脑设备采购", "尾气处理液", "设备采购", "询价", "大修服务", "比价", "购置空调", "空调维修", "购买", "数据采集", "压力容器", "塑钢窗", "维保", "化学显微镜", "生活电器", "弱电改造", "炮弹销毁", "运维", "显示屏租赁", "配电", "绩效评价", "空调", "家具", "红外光谱仪", "气瓶集装箱", "电梯", "资金存放银行", "车辆及保管服务", "分行业配置", "配套租赁", "无损检测", "检查修复", "风机维修", "维护作业", "电源扩容", "防护器材", "信号机", "密钥算法", "绿植租摆", "配备家电", "保险服务", "饭堂厨具", "光谱仪", "布展项目", "电力增容", "散热器", "大数据科研平台", "声学风洞", "安全防控体系", "文化氛围", "报警器", "粉刷", "劳务", "货物采购", "混凝土采购", "电缆采购", "职场改良", "课程项目", "乐高课程", "专利服务", "职场建设", "绿植鲜花", "机房建设", "新能力建设项目", "用水指标申请", "座椅采购", "试验装置", "废物处置", "电源租赁服务", "砍伐树木", "株树木", "分布式电源", "光伏发电", "输变电工程", "千伏线路工程", "购置", "电缆改造", "喷雾灭火系统", "面包车", "树木砍伐", "临时用水指标", "云服务平台", "智能平台", "水影响评价", "分析设备", "以太网芯片", "增蛋鸡料", "分布式光伏", "主变增容", "送出工程", "能力建设与提升", "充电桩项目", "千伏电网", "光缆改造", "分布式发电", "生产技改", "开关改造", "计量生产", "计量器具", "千伏开关柜", "电压互感器", "光伏项目", "机械水表", "智能化应用", "公共服务平台", "树木伐除", "杨树砍伐" };

    private static final String UPDATE_SQL = "INSERT INTO all_item_data( task_id, contentid, title, content, update_time, progid, url, stage, province, city, country, item_linkman, item_linkphone, item_type, owner_unit, item_amount, keyword, code, keyword_term) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SELECT_COMPANY_NEW  = "SELECT new FROM company_for where new like ? and company_type = ?";


    private void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = zhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList(ConstantBean.SELECT_TIME_ONE_NOW_02, noticeMQ.getContentid().toString());
        if (maps != null &&maps.size() > 0){
            Map<String, Object> resultMap = maps.get(0);
            ItemInfo itemInfo = getItemDataByZB(resultMap);
            if (itemInfo != null) {

                boolean flag01 = false;

                String itemTitle = itemInfo.getItemTitle();

                for (String black : blacks) {
                    if (itemTitle.contains(black)){
                        return;
                    }
                }

                String linkman = "";
                String linkPhone = "";
                try {
                    Map<String, Object> map = zhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
                    if (map != null){
                        linkman = map.get("relation_name") != null ? map.get("relation_name").toString() : "";
                        linkPhone = map.get("relation_way") != null ? map.get("relation_way").toString() : "";
                    }
                } catch (Exception e){
                    log.info("调取中台接口异常");
                }
                ExtractNature extractNature = NatureExtractor.exrtractNature(itemInfo.getItemContent());
                ExtractInvestAmount extractInvestAmount = InvestAmountExtractor.extractInvestAmount(itemInfo.getItemContent());
                ExtractOwner extractOwner = PropertyOwnerExtractor.exrtractNature(itemInfo.getItemContent());

                String itemType = extractNature.getNature();
                String itemAmount = extractInvestAmount.getInvestAmount();
                String ownerUnit = extractOwner.getPropertyOwner();

                if (StringUtils.isNotBlank(ownerUnit)){
                    List<Map<String, Object>> maps1 = bdJdbcTemplate.queryForList(SELECT_COMPANY_NEW, "%" + ownerUnit + "%", 1);
                    if (maps1 != null && maps1.size() > 0){
                        noticeMQ.setKeyword(maps1.get(0).get("new").toString());
                        flag01 = true;
                    }
                }

                if (flag01){
                    String code = "";
                    String keyword_term = "";
                    List<Map<String, Object>> twoMaps = bdJdbcTemplate.queryForList("select old, type from company_for where new = ? and company_type = ?", noticeMQ.getKeyword(), 3);
                    if (twoMaps != null && twoMaps.size() > 0){
                        String old = twoMaps.get(0).get("old").toString();
                        Integer type = (Integer) twoMaps.get(0).get("type");
                        if (type == 1){
                            keyword_term =  "对外投资";
                        } else if (type == 2){
                            keyword_term =  "分支机构";
                        } else if (type == 0){
                            keyword_term = "自身";
                        }
                        code = old;
                    }
                    log.info("插入到数据库中contentid: {}", itemInfo.getItemId());
                    bdJdbcTemplate.update(UPDATE_SQL, noticeMQ.getTaskId(), itemInfo.getItemId(), itemInfo.getItemTitle(), itemInfo.getItemContent(), itemInfo.getItemPublishTime(),itemInfo.getItemStage(),
                            itemInfo.getItemQianlimaUrl(), itemInfo.getItemType(), itemInfo.getAreaProvince(), itemInfo.getAreaCity(), itemInfo.getAreaCountry(), linkman, linkPhone, itemType, ownerUnit, itemAmount, noticeMQ.getKeyword(), code, keyword_term);

                }
            }
        }
    }



    private ItemInfo getItemDataByZB(Map<String, Object> itemDataMap) {
        ItemInfo itemInfo = new ItemInfo();
        if (itemDataMap != null){
            // 获取省、市代码
            String areaid = itemDataMap.get("areaid").toString();
            Area area = areaMap.get(Integer.valueOf(areaid));
            if (area != null) {
                Integer province = -1;
                Integer city = -1;
                String arrparentid = area.getArrparentid().trim();
                String[] temp = arrparentid.split(",");
                if (temp.length == 1) {
                    itemInfo.setAreaProvince(area.getName());
                } else if (temp.length == 2) {
                    province = Integer.valueOf(temp[1]);
                    itemInfo.setAreaProvince(areaMap.get(Integer.valueOf(province)).getName());
                    itemInfo.setAreaCity(area.getName());
                } else if (temp.length == 3) {
                    province = Integer.valueOf(temp[1]);
                    city = Integer.valueOf(temp[2]);
                    itemInfo.setAreaProvince(areaMap.get(Integer.valueOf(province)).getName());
                    itemInfo.setAreaCity(areaMap.get(Integer.valueOf(city)).getName());
                    itemInfo.setAreaCountry(area.getName());
                }
            }
            // 获取原文信息
            String contentid = itemDataMap.get("contentid").toString();
            if (StringUtils.isNotBlank(contentid)){
                List<Map<String, Object>> contentList = gwJdbcTemplate.queryForList(ConstantBean.SELECT_ITEM_CONTENT_BY_CONTENTID, contentid);
                if (contentList != null && contentList.size() > 0){
                    String content = contentList.get(0).get("content").toString();
                    itemInfo.setItemContent(content);
                }
            }
            // 获取发布时间
            String updateTimeStr = itemDataMap.get("updatetime").toString();
            if (StringUtils.isNotBlank(updateTimeStr)){
                String itemPublishTime = DateFormatUtils.format(new Date((Long.valueOf(updateTimeStr)) * 1000), "yyyy-MM-dd HH:mm:ss");
                itemInfo.setItemPublishTime(itemPublishTime);
            }
            itemInfo.setItemId(contentid);
            itemInfo.setAreaId(areaid);
            itemInfo.setItemTitle(itemDataMap.get("title").toString());
            itemInfo.setItemType(itemDataMap.get("catid").toString());
            itemInfo.setItemQianlimaUrl(itemDataMap.get("url").toString());
            itemInfo.setItemStage(itemDataMap.get("progid").toString());
        }
        return itemInfo;
    }

}
