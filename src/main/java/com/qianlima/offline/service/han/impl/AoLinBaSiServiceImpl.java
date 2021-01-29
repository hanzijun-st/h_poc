package com.qianlima.offline.service.han.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.middleground.NewZhongTaiService;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.PocService;
import com.qianlima.offline.service.han.AoLinBaSiService;
import com.qianlima.offline.util.ContentSolr;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.QianlimaZTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by hanzijun on 2021/1/12.
 */
@Service
@Slf4j
public class AoLinBaSiServiceImpl implements AoLinBaSiService {
    @Autowired
    private ContentSolr contentSolr;

    @Autowired
    private PocService pocService;

    @Autowired
    private NewZhongTaiService newZhongTaiService;


    @Override
    public void getAoLinBaSiAndSave() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词
        String[] keyWords = {"胃肠镜","支气管镜","腹腔镜","宫腔镜","鼻咽喉镜","胆道镜","支气管镜","胃镜","肠镜","超声镜","十二指肠镜","支气管镜","小肠镜","腹腔镜","胸腔镜","胆道镜","支气管镜","内窥镜","胃肠镜","耳鼻喉镜","胆道镜","腹腔镜","宫腔镜","腹腔镜","宫腔电切","内窥镜","腹腔镜","宫腔电切镜"};

        for (String str : keyWords) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201128 TO 20201231] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\" ", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            listAll.add(data);
                            data.setKeyword(str);
                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                list.add(data);
                                dataMap.put(data.getContentid().toString(), "0");
                            }
                        }
                    }
                }
            }));
        }

        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();


        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");


        ArrayList<String> arrayList = new ArrayList<>();
        for (String key : keyWords) {
            arrayList.add(key);
        }

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : list) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());



        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> pocService.getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
    }

    @Override
    public String getUrlOriginalLink(String num) {
        String url="http://datafetcher.intra.qianlima.com/dc/bidding/fromurl";
        String[] contentIds ={"209327976","209285263","209340707","209363720","209362683",
                "209359682","209302419","209271602","209271687","209348803","209267201",
                "209268068","209372322","209331860","209326536","209261018","209353609",
                "209360570","209314573","209297358","209350312","209285021","209279580",
                "209316310","209253635","209288423","209280023","209269876","209330939","209340849",
                "209300757","209259692","209349626","209379840","209379661","209355574","209312227",
                "209270638","209374166","209366916","209337642","209372926","209382730","209265272",
                "209273394","209332973","209361198","209310974","209387953","209346480","209322341",
                "209351564","209307711","209340037","209368309","209344279","209303373","209378057",
                "209348118","209340321","209328813","209298825","209282082","209295380","209367753",
                "209358100","209326829","209346985","209344056","209369346","209338772","209268684",
                "209305900","209301321","209363079","209374522","209288566","209384622","209289574",
                "209338150","209365423","209302223","209364264","209254053","209263710","209381299",
                "209268337","209374276","209318769","209365067","209310292","209373284","209318381",
                "209289141","209374932","209317584","209325747","209305013","209280378","209335841",
                "209299033","209359559","209284972","209294885","209257513","209282618","209372875",
                "209271507","209356158","209323159","209373268","209287878","209340439","209303873","209263889","209370271",
                "209344696","209301271","209350285","209300911","209293415","209373910","209279829","209264389",
                "209339975","209259061","209265006","209288592","209314937","209360546","209363340","209363601","209336616",
                "209351075","209356230","209265160","209307120","209388006","209288688","209334885","209372143",
                "209263232","209316227","209296080","209309692","209322933","209344894","209260109","209325545","209296462",
                "209354127","209323085","209322626","209310216","209301356","209313740","209320083","209351359","209374855",
                "209376408","209272582","209324317","209349340","209325628","209295883","209344149","209383143","209268570",
                "209379214","209349050","209365584","209353059","209341537","209286311","209368581","209349689","209326871",
                "209331423","209297282","209373030","209282796","209350627","209299542","209318605","209259034","209327372",
                "209372528","209269830","209298947","209316210","209265534","209329581","209262714","209313462","209358763",
                "209373563","209332486","209348102","209291847","209330005"};

        String[] contentIds2 ={"183029694","182986447","183029761","183072163","182967426","183036147","182982661","182988152","183071624","183011234","183001004","183007305","182939571","183047733","183026967","183053979","183036515","183018957","183052315","183059454","182949001","183067306","182982387","183042627","183055167","183074920","183080411","183037388","183053524","182967410","182974333","183084499","182976015","182946791","182986169","183024758","182888120","183078147","182972533","183059174","182996255","182948752","183002700","183044111","183055602","183029839","183032765","183052184","183070660","182980932","183056782","182999205","182955081","182956464","182890034","182923963","183045258","183056392","183063794","182994046","182966756","183019880","183010386","182963662","183002931","182993904","182942226","183035740","182980517","183021386","183050442","182945578","182967389","182983890","183032551","182994058","183047512","183071652","183024608","183064515","182959294","182952854","182982303","183084505","183075548","182951534","183047748","183086260","183026097","183066238","182962163","182985799","183075568","183000898","182959009","183057876","182988483","182995229","183048073","133207528","133200508","133101177","133076914","133212950","133208432","133122188","133089139","133141775","133157439","133237290","133092669","133222104","133131631","133122323","133244866","133166626","133119915","133160482","133064958","133156869","133185477","133241763","133156793","133242559","133096468","133242089","133131742","133106767","133109568","133116898","133236575","133242078","133205140","133200517","133237366","133178286","133152464","133141792","133121330","133199064","133151435","133208575","133106795","133176458","133162854","133148826","133195156","133159804","133254947","133093719","133124980","133122277","133237346","133237337","133241753","133139692","133155330","133202446","133141811","133179770","133207347","133152437","133122108","133240215","133208796","133192267","133131607","133221266","133251223","133080796","133228868","133205051","133208680","133142069","133229654","133234950","133122085","133248211","133238919","133229511","133207296","133161808","133246519","133193506"};
        String[] contentIds3 ={"159121146","159104520","159007356","159069071","159049635","159052579","158957666","158983920","159059704","159121481","158877956","158934566","159045414","159092534","159007044","159038734","159103273","158935978","159057575","158926212","158960159","159035066","158989486","158935196","159015786","158996156","159030662","158932129","159091979","159073476","159130761","159132151","159132923","159049645","158973856","159067760","158967163","159058493","159025492","158967492","158987626","159042277","158968656","159107610","159010006","158987981","158936098","158958510","159060556","159046910","158987191","159068741","158986061","158922426","159032254","158945616","158988020","158920834","158907912","159004663","159074249","159033250","159042540","158980086","159115195","158879528","158943438","159007928","159026894","159020847","159058186","158861523","158866005","158978925","159056746","159032770","158993886","159118516","158926233","159058147","158927240","158991141","159056647","158923727","158934011","158910197","159005111","159069106","159112321","158944147","159020918","158978385","159096873","158939376","159027662","159056252","159053496","158935332","159058443","159027959","159086574","159027821","158952391","159037922","158942616","159012592","159056976","159059182","159132205","159051526","158937089","158922871","158951718","159121486","159004190","158907921","158920335","158877583","158936294","159020266","158979092","159009901","158990239","159006373","159066172","159037317","159110936","159036051","159139781","159013929","159044272","159060546","159069499","158989752","158904451","158966415","159049523","158984026","158874956","158922084","159012203","159036593","158941453","159111964","159087278","159039145","158995423","159041592","158863514","159014332","159055862","159022778","159115650","159123397","158976570","158960390","159089400","159014182","159116519","159072236","159080367","159018001","158996529","159035716","159035284","159066644","159018739","159038876","158991020","159110015","159021351","158926734","159096593","159053848","158960084","159103141","159073046","159063628","159113402","159001330","159029920","159031118","158953083","158940160","159038718","158992482","159065264","159106149","158879351","158929093","159061818","159115631","159014466","159104147","159116602","158956068","159007309","159071324","159059251","158987551"};

        String[] strs = null;
        if ("1".equals(num)){
            strs = contentIds;
        }else if("2".equals(num)){
            strs = contentIds2;
        }else if ("3".equals(num)){
            strs = contentIds3;
        }else {
            System.out.println("参数异常-请传num为 1或2或3");
            return "参数异常-请传num为 1或2或3";
        }
        List<String> listStr = Arrays.asList(strs);
        List<String> jsonList = new ArrayList<>();
        for (String s : listStr) {
            String fromUrl = QianlimaZTUtil.getFromUrl(url, s);
            System.out.println(s+"-"+fromUrl);
            jsonList.add(s+"-"+fromUrl);
        }
        return JSONUtils.toJSONString(jsonList);
    }

    @Override
    public void getJdgl() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //读取配置文件中的黑词
            List<String> blacks = LogUtils.readRule("blockKeys");
            //关键词
            String[] keyWords = {"租赁","出租","租用"};
            String[] keyWords2 = {"班车","通勤车","公务车","商务车","SUV型","越野车","公务用车","新能源汽车"};

            //String string = "yyyymmdd:["+time1 + " TO "+time2 + "] AND (progid:"+progidStr+")"+" AND catid:[* TO 100] AND "+titleOrAllcontent;
            for (String str : keyWords) {
                for (String str2 : keyWords2) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200601 TO 20201231] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + str + "\"  AND allcontent:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(str+"&"+str2);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            list.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
                                    }
                                }
                            }
                        }
                    }));
                }
            }


            //关键词c

            String[] keyWords3 = {"车辆租赁","商务车租赁","公务车租赁","汽车租赁","班车租赁","通勤车租赁","新能源汽车租赁"};

            for (String str : keyWords3) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200601 TO 20201231] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\" ", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blacks) {
                                    if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }

            for (Future future1 : futureList1) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService1.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService1.shutdown();


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();
            for (String key : keyWords) {
                for (String str2 : keyWords2) {
                    arrayList.add(key+"&"+str2);
                }
            }

            for (String key3 :keyWords3){
                arrayList.add(key3);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : list) {
                    String keyword = noticeMQ.getKeyword();
                    if (keyword.equals(str)) {
                        total++;
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());



            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();

                List<Map<String,Object>> listMap = new ArrayList<>();
                for (NoticeMQ content : list) {
                   // futureList.add(executorService.submit(() -> pocService.getDataFromZhongTaiAndSave(content)));
                    futureList.add(executorService.submit(() -> pocService.getDataFromZhongTaiAndList(content)));
                }
                if (listMap !=null && listMap.size() >0){
                    newZhongTaiService.saveIntoMySqlByList(listMap);
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
                System.out.println("==========================================此程序运行结束========================================");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getZw() {
        //读取配置文件中的黑词
        try {
            List<String> blacks = LogUtils.readRule("blockKeys");


            ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
            List<NoticeMQ> list = new ArrayList<>();//去重后的数据
            List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
            HashMap<String, String> dataMap = new HashMap<>();
            List<Future> futureList1 = new ArrayList<>();

            //关键词
            String[] keyWords = {"腹腔镜"};

            for (String str : keyWords) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200101 TO 20201231] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\" ", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            boolean flag = true;
                            for (String black : blacks) {
                                if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag){
                                listAll.add(data);
                                data.setKeyword(str);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }));
            }

            for (Future future1 : futureList1) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService1.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService1.shutdown();


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();
            for (String key : keyWords) {
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : list) {
                    String keyword = noticeMQ.getKeyword();
                    if (keyword.equals(str)) {
                        total++;
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());



            /*if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> pocService.getDataFromZhongTaiAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
