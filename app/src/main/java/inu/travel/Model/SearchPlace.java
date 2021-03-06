package inu.travel.Model;

import java.util.List;

/**
 * Created by jingyu on 2016-01-25.
 */
public class SearchPlace {
    public String contenttypeid;
    public String contentid;
    public String mapx;
    public String mapy;
    public String addr1;
    public String addr2;
    public String areacode;
    public String cat1;
    public String cat2;
    public String cat3;
    public String dist;
    public String firstimage;
    public String firstimage2;
    public String readcount; //조회수
    public String sigungucode;
    public String tel;
    public String title;
    public String homepage;
    public String overview;

    //    <addr1>서울특별시 중구 명동8가길 32</addr1>
//    <addr2>(충무로2가)</addr2>
//    <areacode>1</areacode>
//    <cat1>A04</cat1>
//    <cat2>A0401</cat2>
//    <cat3>A04010600</cat3>
//    <contentid>984586</contentid>
//    <contenttypeid>38</contenttypeid>
//    <dist>888</dist>
//    <firstimage>http://tong.visitkorea.or.kr/cms/resource/36/1009936_image2_1.jpg</firstimage> //500,333
//    <firstimage2>http://tong.visitkorea.or.kr/cms/resource/36/1009936_image3_1.jpg</firstimage2>//150,100
//    <mapx>126.9868259565</mapx>
//    <mapy>37.5616494080</mapy>
//    <readcount>8991</readcount>
//    <sigungucode>24</sigungucode>
//    <tel>02-753-6372, 778-1110</tel>
//    <title>가나 안경원 (명동2호점)</title>
    public String toString() {
        return "Lat : " + this.mapx +
                "\nLon : " + this.mapy +
                "\ntitle : " + this.title +
                "\ncontentID : " + this.contentid;
    }
}
