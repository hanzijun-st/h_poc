package com.qianlima.offline.service.han;

public interface TestBeiDengService {
    /**
     *  贝登-第四回合
     * @param type
     * @param date
     * @param progidStr
     */
    void getBeiDeng4(Integer type, String date, String progidStr) throws Exception;

    /**
     *  贝登-第四回合规则2
     * @param type
     * @param date
     * @param progidStr
     */
    void getBeideng4_2(Integer type, String date, String progidStr) throws Exception;

    /**
     *  贝登-第四回规则三
     * @param type
     * @param date
     * @param progidStr
     */
    void getBeideng3(Integer type, String date, String progidStr) throws Exception;

    /**
     *  去黑词
     * @param type
     * @param date
     * @param progidStr
     */
    void getBeideng4_1(Integer type, String date, String progidStr) throws Exception;
}