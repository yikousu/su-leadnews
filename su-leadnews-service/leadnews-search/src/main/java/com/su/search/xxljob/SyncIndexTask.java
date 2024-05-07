package com.su.search.xxljob;

import com.alibaba.fastjson.JSON;
import com.su.feign.article.IArticleClient;
import com.su.model.common.search.vos.SearchArticleVo;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 同步索引任务【将article中数据导入到ES中】
 *
 *  search远程调用article
 *
 *  1. 定时导入
 *  2. 分片导入【大分页+小分页】   集群
 */
@Component //加入容器管理
public class SyncIndexTask {

    @Autowired
    private IArticleClient articleClient;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //线程池
    public static ExecutorService pool = Executors.newFixedThreadPool(10);

    /***
     * 同步索引任务
     *  1)当数量大于100条的时候，才做分片导入，否则只让第1个导入即可
     *      A:查询所有数据量 ->searchTotal total>100 [判断当前分片不是第1个分片]
     *      第N个分片执行数据处理范围-要计算   确定当前分片处理的数据范围  limit #{index},#{size}
     *                                                                [index-范围]
     *
     *      B:执行分页查询-需要根据index判断是否超过界限，如果没有超过界限，则并开启多线程，分页查询，将当前分页数据批量导入到ES
     *
     *      C:在xxl-job中配置作业-策略：分片策略
     *
     */
    @XxlJob(value = "syncIndex")
    public void syncIndex() {
        //1、获取任务传入的参数   {"minSize":100,"size":10}
        String jobParam = XxlJobHelper.getJobParam();

        Map<String, Integer> jobData = JSON.parseObject(jobParam, Map.class);
        int minSize = jobData.get("minSize"); //分片处理的最小总数据条数
        int size = jobData.get("size"); //分页查询的每页条数   小分页

        //2、查询需要处理的总数据量
        Long total = articleClient.searchTotal();
        //3、判断当前分片是否属于第1片，不属于，则需要判断总数量是否大于指定的数据量[minSize]，大于，则执行任务处理，小于或等于，则直接结束任务
        int cn = XxlJobHelper.getShardIndex(); //当前节点的下标
        if (total <= minSize && cn != 0) {
            //结束
            return;
        }

        //4、执行任务   [index-范围]   大的分片分页处理
        //4.1：节点个数
        int n = XxlJobHelper.getShardTotal();
        //4.2：当前节点处理的数据量
        int count = (int) (total % n == 0 ? total / n : (total / n) + 1);
        //4.3：确定当前节点处理的数据范围
        //从下标为index的数据开始处理  limit #{index},#{count}
        int indexStart = cn * count;
        int indexEnd = cn * count + count - 1; //最大的范围的最后一个数据的下标
        //5.小的分页查询和批量处理
        int index = indexStart; //第1页的index

        System.out.println("分片个数是【" + n + "】,当前分片下标【" + cn + "】，处理的数据下标范围【" + indexStart + "-" + indexEnd + "】");
        do {
            //=============================================小分页================================
            //5.1:分页查询
            //5.2:将数据导入ES
            push(index, size, indexEnd);

            //5.3:是否要查询下一页 index+size
            index = index + size;
        } while (index <= indexEnd);
    }

    /**
     * 数据批量导入
     *
     * @param index
     * @param size
     * @param indexEnd
     * @throws IOException
     */
    public void push(int index, int size, int indexEnd) {

        pool.execute(() -> {

            System.out.println("当前线程"+ Thread.currentThread().getId()+"处理的分页数据是【index=" + index + ",size=" + (index + size > indexEnd ? indexEnd - index + 1 : size) + "】");
            //1)查询数据库数据
            List<SearchArticleVo> searchArticleVos = articleClient.searchPage(index, index + size > indexEnd ? indexEnd - index + 1 : size);  //size可能越界
            // 第1页  index=0
            //       indexEnd=6
            // 第2页  index=5
            //       indexEnd-index+=2
            //2)创建BulkRequest - 刷新策略
            BulkRequest bulkRequest = new BulkRequest()
                    //刷新策略-立即刷新
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            for (SearchArticleVo searchArticleVo : searchArticleVos) {
                //初始化单词自动补全的值
                searchArticleVo.initSuggestion();
                //A:创建XxxRequest
                IndexRequest indexRequest = new IndexRequest("hmtt")
                        //B:向XxxRequest封装DSL语句数据
                        .id(searchArticleVo.getId().toString())
                        .source(com.alibaba.fastjson.JSON.toJSONString(searchArticleVo), XContentType.JSON);

                //3)将XxxRequest添加到BulkRequest
                bulkRequest.add(indexRequest);
            }

            //4)使用RestHighLevelClient将BulkRequest添加到索引库
            if (searchArticleVos != null && searchArticleVos.size() > 0) {
                try {
                    restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
