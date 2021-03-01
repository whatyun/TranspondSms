package com.jds.wk.api.annotation;

import org.apache.commons.lang3.StringUtils;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;

class RuleLine{
    //开头有几个空格
    int headSpaceNum=0;
    RuleLine beforeRuleLine;
    RuleLine nextRuleLine;
    RuleLine parentRuleLine;
    RuleLine childRuleLine;
    //and or
    String conjunction;
    public static List<String> CONJUNCTION_LIST=new ArrayList<String>();
    public static final String CONJUNCTION_AND="并且";
    public static final String CONJUNCTION_OR="或者";

    static {
        CONJUNCTION_LIST.add("and");
        CONJUNCTION_LIST.add("or");
        CONJUNCTION_LIST.add("并且");
        CONJUNCTION_LIST.add("或者");
    }
    //手机号 短信内容
    String field;
    public static List<String> FILED_LIST=new ArrayList<String>();
    public static final String FILED_PHONE_NUM="手机号";
    public static final String FILED_MSG_CONTENT="短信内容";
    static {
        FILED_LIST.add("手机号");
        FILED_LIST.add("短信内容");
    }
    // 是否
    String sure;
    public static List<String> SURE_LIST=new ArrayList<String>();
    public static final String SURE_YES="是";
    public static final String SURE_NOT="不是";
    static {
        SURE_LIST.add("是");
        SURE_LIST.add("不是");
    }

    String check;
    public static List<String> CHECK_LIST=new ArrayList<String>();
    public static final String CHECK_EQUALS="相等";
    public static final String CHECK_CONTAIN="包含";
    public static final String CHECK_START_WITH="开头";
    public static final String CHECK_END_WITH="结尾";
    static {
        CHECK_LIST.add("相等");
        CHECK_LIST.add("包含");
        CHECK_LIST.add("开头");
        CHECK_LIST.add("结尾");
    }

    String value;

    //字段分支
    public boolean checkMsg(Msg msg){

        //检查这一行和上一行合并的结果是否命中
        boolean mixChecked=false;

        //先检查规则是否命中
        switch (this.field){
            case FILED_PHONE_NUM:
                mixChecked= checkValue(msg.from);
                break;
            case FILED_MSG_CONTENT:
                mixChecked= checkValue(msg.content);
                break;
            default:
                break;

        }

        //整合肯定词
        switch (this.sure){
            case SURE_YES:
                mixChecked= mixChecked;
                break;
            case SURE_NOT:
                mixChecked= !mixChecked;
                break;
            default:
                mixChecked=false;
                break;

        }

        System.out.println("rule:"+this+" checkMsg:"+msg+" checked:"+mixChecked);
        return mixChecked;

    }

    //内容分支
    public boolean checkValue(String msgValue){
        boolean checked=false;

        switch (this.check){
            case CHECK_EQUALS:
                checked=this.value.equals(msgValue);
                break;
            case CHECK_CONTAIN:
                checked=this.value.contains(msgValue);
                break;
            case CHECK_START_WITH:
                checked=this.value.startsWith(msgValue);
                break;
            case CHECK_END_WITH:
                checked=this.value.endsWith(msgValue);
                break;
            default:
                break;
        }
        System.out.println("checkValue "+msgValue+" "+this.check+" "+this.value+" checked:"+checked);

        return checked;

    }

    public RuleLine(String line,int linenum,RuleLine beforeRuleLine) throws Exception {
        System.out.println("----------"+linenum+"-----------------");
        System.out.println(line);
        //规则检验：
        //并且 是 手机号 相等 10086
        //[并且, 是, 手机号, 相等, 10086]
        //  并且 是 内容 包含 asfas
        //[, , 并且, 是, 内容, 包含, asfas]

        //处理头空格数用来确认跟上一行节点的相对位置：是同级还是子级
        //处理4个字段，之后的全部当做value

        //标记3个阶段
        boolean isCountHeading=false;
        boolean isDealMiddel=false;
        boolean isDealValue=false;

        //用于保存4个中间体： 并且, 是, 内容, 包含
        List<String> middleList=new ArrayList<>(4);
        //保存每个中间体字符串
        StringBuilder buildMiddleWord= new StringBuilder();
        StringBuilder valueBuilder= new StringBuilder();

        for(int i=0;i<line.length();i++) {
            String w= String.valueOf(line.charAt(i));
            System.out.println("walk over:"+w);

            //控制阶段
            //开始处理头
            if(i==0){
                if(StringUtils.isBlank(w)){
                    System.out.println("start to isCountHeading:");

                    isCountHeading=true;
                }else{
                    //直接进入处理中间体阶段
                    isCountHeading=false;
                    isDealMiddel=true;
                    System.out.println("start to isDealMiddel:");
                }

            }
            //正在数空格头，但是遇到非空格，阶段变更:由处理空头阶段  变为  处理 中间体阶段
            if(isCountHeading && (!StringUtils.isBlank(w))){
                System.out.println("isCountHeading to isDealMiddel:");

                isCountHeading=false;
                isDealMiddel=true;
            }

            //正在处理中间体，中间体数量够了，阶段变更：由处理中间体  变为  处理 value
            if(isDealMiddel && middleList.size()==4){
                System.out.println("isDealMiddel done middleList:"+middleList);
                System.out.println("isDealMiddel to isDealValue:");
                isDealMiddel=false;
                isDealValue=true;
            }

            System.out.println("isCountHeading:"+isCountHeading);
            System.out.println("isDealMiddel:"+isDealMiddel);
            System.out.println("isDealValue:"+isDealValue);

            if(isCountHeading){
                if(StringUtils.isBlank(w)){
                    System.out.println("headSpaceNum++:"+headSpaceNum);
                    headSpaceNum++;
                }
            }

            if(isDealMiddel){
                //遇到空格
                if(StringUtils.isBlank(w)){
                    if(buildMiddleWord.length() == 0){
                        throw new Exception(linenum+"行：语法错误不允许出现连续空格！");
                    }else{
                        //生成了一个中间体
                        middleList.add(buildMiddleWord.toString());
                        System.out.println("get Middle++:"+buildMiddleWord.toString());

                        buildMiddleWord = new StringBuilder();
                    }
                }else{
                    //把w拼接到中间体上
                    buildMiddleWord.append(w);
                    System.out.println("buildMiddleWord length:"+buildMiddleWord.length()+"buildMiddleWord:"+buildMiddleWord.toString());

                }
            }

            if(isDealValue){
                //把余下的所有字符都拼接给value
                valueBuilder.append(w);
            }

        }
        System.out.println("isDealValue done valueBuilder:"+valueBuilder.toString());


        if(middleList.size()!=4){
            throw new Exception(linenum+"行配置错误：每行必须有4段组成，例如： 并且 手机号 是 相等 ");
        }


        //规则对齐
        if(beforeRuleLine!=null){
            System.out.println("beforeRuleLine :"+beforeRuleLine);
            System.out.println("thisRuleLine :"+this);

            //同级别
            if(headSpaceNum==beforeRuleLine.headSpaceNum){
                System.out.println("同级别");
                this.beforeRuleLine=beforeRuleLine;
                beforeRuleLine.nextRuleLine=this;
            }
            //子级
            if(headSpaceNum-1==beforeRuleLine.headSpaceNum){
                System.out.println("子级");
                this.parentRuleLine=beforeRuleLine;
                beforeRuleLine.childRuleLine=this;
            }
            //查找父级别
            if(headSpaceNum<beforeRuleLine.headSpaceNum){
                //匹配到最近一个同级
                RuleLine fBeforeRuleLine=beforeRuleLine.getBeforeRuleLine();
                if (fBeforeRuleLine==null){
                    fBeforeRuleLine=beforeRuleLine.getParentRuleLine();
                }

                while (fBeforeRuleLine!=null){
                    System.out.println("fBeforeRuleLine"+fBeforeRuleLine);

                    //查找到同级别
                    if(headSpaceNum==fBeforeRuleLine.headSpaceNum){
                        System.out.println("父级别");
                        this.beforeRuleLine=fBeforeRuleLine;
                        fBeforeRuleLine.nextRuleLine=this;
                        break;
                    }else{
                        //向上查找
                        RuleLine testfBeforeRuleLine=fBeforeRuleLine.getBeforeRuleLine();
                        if (testfBeforeRuleLine==null){
                            testfBeforeRuleLine=fBeforeRuleLine.getParentRuleLine();
                        }
                        fBeforeRuleLine=testfBeforeRuleLine;

                    }
                }
            }

        }else{
            System.out.println("根级别");
        }


        this.conjunction=middleList.get(0);
        this.sure=middleList.get(1);
        this.field=middleList.get(2);
        this.check=middleList.get(3);
        this.value=valueBuilder.toString();

        if(!CONJUNCTION_LIST.contains(this.conjunction)){
            throw new Exception(linenum+"行配置错误：连接词只支持："+CONJUNCTION_LIST+" 但提供了"+this.conjunction);
        }
        if(!FILED_LIST.contains(this.field)){
            throw new Exception(linenum+"行配置错误：字段只支持："+FILED_LIST+" 但提供了"+this.field);
        }
        if(!SURE_LIST.contains(this.sure)){
            throw new Exception(linenum+"行配置错误 "+this.sure+" 确认词只支持："+SURE_LIST+" 但提供了"+this.sure);
        }
        if(!CHECK_LIST.contains(this.check)){
            throw new Exception(linenum+"行配置错误：比较词只支持："+CHECK_LIST+" 但提供了"+this.check);
        }

        System.out.println("----------"+linenum+"=="+this);


    }

    @Override
    public String toString() {
        return "RuleLine{" +
                "headSpaceNum='" + headSpaceNum + '\'' +
                "conjunction='" + conjunction + '\'' +
                ", field='" + field + '\'' +
                ", sure='" + sure + '\'' +
                ", check='" + check + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public int getHeadSpaceNum() {
        return headSpaceNum;
    }

    public void setHeadSpaceNum(int headSpaceNum) {
        this.headSpaceNum = headSpaceNum;
    }

    public RuleLine getBeforeRuleLine() {
        return beforeRuleLine;
    }

    public void setBeforeRuleLine(RuleLine beforeRuleLine) {
        this.beforeRuleLine = beforeRuleLine;
    }

    public RuleLine getNextRuleLine() {
        return nextRuleLine;
    }

    public void setNextRuleLine(RuleLine nextRuleLine) {
        this.nextRuleLine = nextRuleLine;
    }

    public RuleLine getParentRuleLine() {
        return parentRuleLine;
    }

    public void setParentRuleLine(RuleLine parentRuleLine) {
        this.parentRuleLine = parentRuleLine;
    }

    public RuleLine getChildRuleLine() {
        return childRuleLine;
    }

    public void setChildRuleLine(RuleLine childRuleLine) {
        this.childRuleLine = childRuleLine;
    }

    public String getConjunction() {
        return conjunction;
    }

    public void setConjunction(String conjunction) {
        this.conjunction = conjunction;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getSure() {
        return sure;
    }

    public void setSure(String sure) {
        this.sure = sure;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}