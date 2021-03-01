package com.jds.wk.api.annotation;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

import static com.jds.wk.api.annotation.RuleLine.CONJUNCTION_AND;
import static com.jds.wk.api.annotation.RuleLine.CONJUNCTION_OR;

@Setter
@Getter
public class ResObject implements Serializable {

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static void main(String[] args) throws Exception {
        String a="并且 是 手机号 相等 10086\n" +
                 " 或者 是 手机号 结尾 哈哈哈\n" +
                 "  并且 是 短信内容 包含 asfas\n" +
                 " 或者 是 手机号 结尾 aaaa\n" +
                 "并且 是 手机号 相等 100861\n"+
                 "并且 是 手机号 相等 100861";

        Msg msg=new Msg("10086","哈哈哈");

        try(Scanner scanner = new Scanner(a)) {

            int linenum=0;
            boolean beforeLine=true;
            boolean isTrue=false;
            RuleLine headRuleLine=null;
            int spaceNum=0;

            RuleLine beforeRuleLine=null;
            RuleLine thisRuleLine;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(linenum+" : "+line);
                //第一行
                if(linenum==0){
                    //第一行不允许缩进
                    if(line.startsWith(" ")){
                        throw new Exception("第一行不允许缩进");
                    }
                }


                // process the line


                beforeRuleLine= ResObject.generateRuleTree(line,linenum,beforeRuleLine);
                if(linenum==0){
                    headRuleLine=beforeRuleLine;
                }

//                isTrue = ruleLine.checkMsg(msg,beforeLine);
//                if(isTrue){
//                    System.out.println("shot!! at"+ruleLine);
//                    break;
//                }else{
//                    //nextline
//                    beforeLine=isTrue;
//                }
                linenum++;
            }

            System.out.println("check:"+checkRuleTree(msg,headRuleLine));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用规则树判断消息是否命中规则
     * Rule节点是否命中取决于：该节点是否命中、该节点子结点（如果有的话）是否命中、该节点下节点（如果有的话）是否命中
     * 递归检查
     */

    public static boolean checkRuleTree(Msg msg,RuleLine currentRuleLine) throws Exception {
        //该节点是否命中
        boolean currentAll=currentRuleLine.checkMsg(msg);
        System.out.println("current:"+currentRuleLine+" checked:"+currentAll);

        //该节点子结点（如果有的话）是否命中
        if(currentRuleLine.getChildRuleLine()!=null){
            System.out.println(" child:"+currentRuleLine.getChildRuleLine());

            //根据情况连接结果
            switch (currentRuleLine.getChildRuleLine().conjunction){
                case CONJUNCTION_AND:
                    currentAll= currentAll && checkRuleTree(msg,currentRuleLine.getChildRuleLine());
                    break;
                case CONJUNCTION_OR:
                    currentAll= currentAll || checkRuleTree(msg,currentRuleLine.getChildRuleLine());
                    break;
                default:
                    throw new Exception("child wrong conjunction");
            }
        }

        //该节点下节点（如果有的话）是否命中
        if(currentRuleLine.getNextRuleLine()!=null){
            System.out.println("next:"+currentRuleLine.getNextRuleLine());
            //根据情况连接结果
            switch (currentRuleLine.getNextRuleLine().conjunction){
                case CONJUNCTION_AND:
                    currentAll= currentAll && checkRuleTree(msg,currentRuleLine.getNextRuleLine());
                    break;
                case CONJUNCTION_OR:
                    currentAll= currentAll || checkRuleTree(msg,currentRuleLine.getNextRuleLine());
                    break;
                default:
                    throw new Exception("next wrong conjunction");
            }
        }

        return currentAll;
    }

    /**
     * 生成规则树
     * 一行代表一个规则
     */

    public static RuleLine generateRuleTree(String line, int lineNum, RuleLine parentRuleLine) throws Exception {
        String[] words=line.split(" ");

        return new RuleLine(line,lineNum,parentRuleLine);
    }

}
