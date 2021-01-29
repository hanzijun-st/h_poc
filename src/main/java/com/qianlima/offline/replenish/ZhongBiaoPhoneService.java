package com.qianlima.offline.replenish;


import com.qianlima.offline.bean.Enterprise;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class ZhongBiaoPhoneService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    //每次取数据的数量
    private Integer appendLimit = 1000000;

    private String code = "1";

    private static final String SELECT_SQL = "SELECT id, content_id as contentid, zhong_biao_unit as zhongbiaounit,link_man,link_phone from jyf_data where id > ? and (LENGTH(link_phone) = 5 OR link_phone is null OR link_phone = '') order by id asc limit ?";

    private static final String UPDATA_SQL = "update jyf_data set code = ?, link_man = ?, link_phone = ? where content_id = ?";

    public String getCompanyAboutHangYe() {
        try {
            boolean idEndFlag = false;
            Integer beginid = 0;
            while (true) {
                idEndFlag = false;
                ExecutorService executorService = Executors.newFixedThreadPool(32);
                List<Future> futureList = new ArrayList<>();
                List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SELECT_SQL, beginid, appendLimit);
                if (maps != null && maps.size() > 0) {
                    log.info("任务查出来了 total：{}", maps.size());
                    if (maps.size() < appendLimit) {
                        idEndFlag = true;
                    }
                    for (Map<String, Object> map : maps) {
                        futureList.add(executorService.submit(() -> insert(map)));
                    }
                    for (Future future1 : futureList) {
                        try {
                            future1.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            executorService.shutdown();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    executorService.shutdown();
                    beginid = (Integer) maps.get(maps.size() - 1).get("id");
                    log.info("任务查出来了的下一页游标beginOid：{}", beginid);
                } else {
                    idEndFlag = true;
                }
                if (idEndFlag) {
                    log.info("任务完成做到最新，跳出");
                    break;
                }
            }
        } catch (Exception e) {
            log.error("任务异常 e:{}", e);
        }
        return "任务完成做到最新";
    }

    private void insert(Map<String, Object> map) {

        String contentid = map.get("contentid").toString();

        if (StringUtils.isBlank((String) map.get("zhongbiaounit"))) {
            return;
        }
        String zhongbiaounit = map.get("zhongbiaounit").toString();

        String legalPersonName = null;
        String phoneNumber = null;

        if (StringUtils.isNotBlank((String) map.get("link_man"))) {
            legalPersonName = map.get("link_man").toString();
        }
        if (StringUtils.isNotBlank((String) map.get("link_phone"))) {
            phoneNumber = map.get("link_phone").toString();
        }

        if (zhongbiaounit.contains("、")) {
            String[] split = zhongbiaounit.split("、");
            zhongbiaounit = split[0];
        } else if (zhongbiaounit.contains("，")) {
            String[] split = zhongbiaounit.split("，");
            zhongbiaounit = split[0];
        }

        Enterprise enterprise = queryForName(zhongbiaounit);
        if (enterprise != null) {
            if (StringUtils.isNotBlank(enterprise.getPhoneNumber())) {
                if (StringUtils.isBlank(legalPersonName)) {
                    legalPersonName = enterprise.getLegalPersonName();
                }
                if (StringUtils.isBlank(phoneNumber) || phoneNumber.contains("*") || phoneNumber.length() <= 5) {
                    phoneNumber = enterprise.getPhoneNumber();
                }
            }
        }
        if (StringUtils.isNotBlank(phoneNumber) || StringUtils.isNotBlank(legalPersonName)) {
            log.info("contentid：{}经过天眼查匹配中标联系方式后, 重新入库", contentid);
            bdJdbcTemplate.update(UPDATA_SQL, code, legalPersonName, phoneNumber, contentid);
        }
    }


    private Enterprise queryForName(String zhongbiaounit) {
        if (StringUtils.isBlank(zhongbiaounit)) {
            return null;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(zhongbiaounit));
        return mongoTemplate.findOne(query, Enterprise.class);
    }
}
