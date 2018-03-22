/***************************************************************\
 *     The skeleton of this class is generated by an automatic *
 * code generator for NC product. It is based on Velocity.     *
\***************************************************************/
package nc.vo.rm.active;
	
import nc.vo.pub.*;

/**
 * <b> 在此处简要描述此类的功能 </b>
 * <p>
 *     在此处添加此类的描述信息
 * </p>
 * 创建日期:
 * @author 
 * @version NCPrj ??
 */
@SuppressWarnings("serial")
public class ActiveVO extends SuperVO {
	private java.lang.String pk_active;
	private java.lang.String pk_group;
	private java.lang.String pk_org;
	private java.lang.String code;
	private java.lang.String name;
	private java.lang.String name2;
	private java.lang.String name3;
	private java.lang.String name4;
	private java.lang.String name5;
	private java.lang.String name6;
	private java.lang.String pk_channel;
	private java.lang.String address;
	private java.lang.Integer plan_reg_num;
	private java.lang.Integer fact_reg_num;
	private java.lang.Integer fact_hire_num;
	private java.lang.Integer fact_checkin_num;
	private nc.vo.pub.lang.UFLiteralDate begindate;
	private nc.vo.pub.lang.UFLiteralDate enddate;
	private java.lang.Object activephoto;
	private java.lang.String sponsor;
	private java.lang.String secondary;
	private java.lang.String pk_manager;
	private java.lang.String phone;
	private nc.vo.pub.lang.UFDouble plan_fee;
	private nc.vo.pub.lang.UFDouble fact_fee;
	private java.lang.Integer activestate;
	private nc.vo.pub.lang.UFLiteralDate state_date;
	private java.lang.String remark;
	private nc.vo.pub.lang.UFDateTime creationtime;
	private java.lang.String creator;
	private nc.vo.pub.lang.UFDateTime modifiedtime;
	private java.lang.String modifier;
	private java.lang.String general_rules;
	private java.lang.Integer dr = 0;
	private nc.vo.pub.lang.UFDateTime ts;

	public static final String PK_ACTIVE = "pk_active";
	public static final String PK_GROUP = "pk_group";
	public static final String PK_ORG = "pk_org";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String NAME2 = "name2";
	public static final String NAME3 = "name3";
	public static final String NAME4 = "name4";
	public static final String NAME5 = "name5";
	public static final String NAME6 = "name6";
	public static final String PK_CHANNEL = "pk_channel";
	public static final String ADDRESS = "address";
	public static final String PLAN_REG_NUM = "plan_reg_num";
	public static final String FACT_REG_NUM = "fact_reg_num";
	public static final String FACT_HIRE_NUM = "fact_hire_num";
	public static final String FACT_CHECKIN_NUM = "fact_checkin_num";
	public static final String BEGINDATE = "begindate";
	public static final String ENDDATE = "enddate";
	public static final String ACTIVEPHOTO = "activephoto";
	public static final String SPONSOR = "sponsor";
	public static final String SECONDARY = "secondary";
	public static final String PK_MANAGER = "pk_manager";
	public static final String PHONE = "phone";
	public static final String PLAN_FEE = "plan_fee";
	public static final String FACT_FEE = "fact_fee";
	public static final String ACTIVESTATE = "activestate";
	public static final String STATE_DATE = "state_date";
	public static final String REMARK = "remark";
	public static final String CREATIONTIME = "creationtime";
	public static final String CREATOR = "creator";
	public static final String MODIFIEDTIME = "modifiedtime";
	public static final String MODIFIER = "modifier";
	public static final String GENERAL_RULES = "general_rules";
			
	/**
	 * 属性pk_active的Getter方法.属性名：活动主键
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getPk_active () {
		return pk_active;
	}   
	/**
	 * 属性pk_active的Setter方法.属性名：活动主键
	 * 创建日期:
	 * @param newPk_active java.lang.String
	 */
	public void setPk_active (java.lang.String newPk_active ) {
	 	this.pk_active = newPk_active;
	} 	  
	/**
	 * 属性pk_group的Getter方法.属性名：所属集团
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getPk_group () {
		return pk_group;
	}   
	/**
	 * 属性pk_group的Setter方法.属性名：所属集团
	 * 创建日期:
	 * @param newPk_group java.lang.String
	 */
	public void setPk_group (java.lang.String newPk_group ) {
	 	this.pk_group = newPk_group;
	} 	  
	/**
	 * 属性pk_org的Getter方法.属性名：所属组织
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getPk_org () {
		return pk_org;
	}   
	/**
	 * 属性pk_org的Setter方法.属性名：所属组织
	 * 创建日期:
	 * @param newPk_org java.lang.String
	 */
	public void setPk_org (java.lang.String newPk_org ) {
	 	this.pk_org = newPk_org;
	} 	  
	/**
	 * 属性code的Getter方法.属性名：活动编码
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getCode () {
		return code;
	}   
	/**
	 * 属性code的Setter方法.属性名：活动编码
	 * 创建日期:
	 * @param newCode java.lang.String
	 */
	public void setCode (java.lang.String newCode ) {
	 	this.code = newCode;
	} 	  
	/**
	 * 属性name的Getter方法.属性名：$map.displayName
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getName () {
		return name;
	}   
	/**
	 * 属性name的Setter方法.属性名：$map.displayName
	 * 创建日期:
	 * @param newName java.lang.String
	 */
	public void setName (java.lang.String newName ) {
	 	this.name = newName;
	} 	  
	/**
	 * 属性name2的Getter方法.属性名：$map.displayName
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getName2 () {
		return name2;
	}   
	/**
	 * 属性name2的Setter方法.属性名：$map.displayName
	 * 创建日期:
	 * @param newName2 java.lang.String
	 */
	public void setName2 (java.lang.String newName2 ) {
	 	this.name2 = newName2;
	} 	  
	/**
	 * 属性name3的Getter方法.属性名：$map.displayName
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getName3 () {
		return name3;
	}   
	/**
	 * 属性name3的Setter方法.属性名：$map.displayName
	 * 创建日期:
	 * @param newName3 java.lang.String
	 */
	public void setName3 (java.lang.String newName3 ) {
	 	this.name3 = newName3;
	} 	  
	/**
	 * 属性name4的Getter方法.属性名：$map.displayName
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getName4 () {
		return name4;
	}   
	/**
	 * 属性name4的Setter方法.属性名：$map.displayName
	 * 创建日期:
	 * @param newName4 java.lang.String
	 */
	public void setName4 (java.lang.String newName4 ) {
	 	this.name4 = newName4;
	} 	  
	/**
	 * 属性name5的Getter方法.属性名：$map.displayName
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getName5 () {
		return name5;
	}   
	/**
	 * 属性name5的Setter方法.属性名：$map.displayName
	 * 创建日期:
	 * @param newName5 java.lang.String
	 */
	public void setName5 (java.lang.String newName5 ) {
	 	this.name5 = newName5;
	} 	  
	/**
	 * 属性name6的Getter方法.属性名：$map.displayName
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getName6 () {
		return name6;
	}   
	/**
	 * 属性name6的Setter方法.属性名：$map.displayName
	 * 创建日期:
	 * @param newName6 java.lang.String
	 */
	public void setName6 (java.lang.String newName6 ) {
	 	this.name6 = newName6;
	} 	  
	/**
	 * 属性pk_channel的Getter方法.属性名：招聘渠道
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getPk_channel () {
		return pk_channel;
	}   
	/**
	 * 属性pk_channel的Setter方法.属性名：招聘渠道
	 * 创建日期:
	 * @param newPk_channel java.lang.String
	 */
	public void setPk_channel (java.lang.String newPk_channel ) {
	 	this.pk_channel = newPk_channel;
	} 	  
	/**
	 * 属性address的Getter方法.属性名：招聘地点
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getAddress () {
		return address;
	}   
	/**
	 * 属性address的Setter方法.属性名：招聘地点
	 * 创建日期:
	 * @param newAddress java.lang.String
	 */
	public void setAddress (java.lang.String newAddress ) {
	 	this.address = newAddress;
	} 	  
	/**
	 * 属性plan_reg_num的Getter方法.属性名：计划招聘人数
	 * 创建日期:
	 * @return java.lang.Integer
	 */
	public java.lang.Integer getPlan_reg_num () {
		return plan_reg_num;
	}   
	/**
	 * 属性plan_reg_num的Setter方法.属性名：计划招聘人数
	 * 创建日期:
	 * @param newPlan_reg_num java.lang.Integer
	 */
	public void setPlan_reg_num (java.lang.Integer newPlan_reg_num ) {
	 	this.plan_reg_num = newPlan_reg_num;
	} 	  
	/**
	 * 属性fact_reg_num的Getter方法.属性名：实际应聘人数
	 * 创建日期:
	 * @return java.lang.Integer
	 */
	public java.lang.Integer getFact_reg_num () {
		return fact_reg_num;
	}   
	/**
	 * 属性fact_reg_num的Setter方法.属性名：实际应聘人数
	 * 创建日期:
	 * @param newFact_reg_num java.lang.Integer
	 */
	public void setFact_reg_num (java.lang.Integer newFact_reg_num ) {
	 	this.fact_reg_num = newFact_reg_num;
	} 	  
	/**
	 * 属性fact_hire_num的Getter方法.属性名：实际录用人数
	 * 创建日期:
	 * @return java.lang.Integer
	 */
	public java.lang.Integer getFact_hire_num () {
		return fact_hire_num;
	}   
	/**
	 * 属性fact_hire_num的Setter方法.属性名：实际录用人数
	 * 创建日期:
	 * @param newFact_hire_num java.lang.Integer
	 */
	public void setFact_hire_num (java.lang.Integer newFact_hire_num ) {
	 	this.fact_hire_num = newFact_hire_num;
	} 	  
	/**
	 * 属性fact_checkin_num的Getter方法.属性名：实际到岗人数
	 * 创建日期:
	 * @return java.lang.Integer
	 */
	public java.lang.Integer getFact_checkin_num () {
		return fact_checkin_num;
	}   
	/**
	 * 属性fact_checkin_num的Setter方法.属性名：实际到岗人数
	 * 创建日期:
	 * @param newFact_checkin_num java.lang.Integer
	 */
	public void setFact_checkin_num (java.lang.Integer newFact_checkin_num ) {
	 	this.fact_checkin_num = newFact_checkin_num;
	} 	  
	/**
	 * 属性begindate的Getter方法.属性名：开始日期
	 * 创建日期:
	 * @return nc.vo.pub.lang.UFLiteralDate
	 */
	public nc.vo.pub.lang.UFLiteralDate getBegindate () {
		return begindate;
	}   
	/**
	 * 属性begindate的Setter方法.属性名：开始日期
	 * 创建日期:
	 * @param newBegindate nc.vo.pub.lang.UFLiteralDate
	 */
	public void setBegindate (nc.vo.pub.lang.UFLiteralDate newBegindate ) {
	 	this.begindate = newBegindate;
	} 	  
	/**
	 * 属性enddate的Getter方法.属性名：结束日期
	 * 创建日期:
	 * @return nc.vo.pub.lang.UFLiteralDate
	 */
	public nc.vo.pub.lang.UFLiteralDate getEnddate () {
		return enddate;
	}   
	/**
	 * 属性enddate的Setter方法.属性名：结束日期
	 * 创建日期:
	 * @param newEnddate nc.vo.pub.lang.UFLiteralDate
	 */
	public void setEnddate (nc.vo.pub.lang.UFLiteralDate newEnddate ) {
	 	this.enddate = newEnddate;
	} 	  
	/**
	 * 属性activephoto的Getter方法.属性名：活动图片
	 * 创建日期:
	 * @return java.lang.Object
	 */
	public java.lang.Object getActivephoto () {
		return activephoto;
	}   
	/**
	 * 属性activephoto的Setter方法.属性名：活动图片
	 * 创建日期:
	 * @param newActivephoto java.lang.Object
	 */
	public void setActivephoto (java.lang.Object newActivephoto ) {
	 	this.activephoto = newActivephoto;
	} 	  
	/**
	 * 属性sponsor的Getter方法.属性名：主办单位
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getSponsor () {
		return sponsor;
	}   
	/**
	 * 属性sponsor的Setter方法.属性名：主办单位
	 * 创建日期:
	 * @param newSponsor java.lang.String
	 */
	public void setSponsor (java.lang.String newSponsor ) {
	 	this.sponsor = newSponsor;
	} 	  
	/**
	 * 属性secondary的Getter方法.属性名：协办单位
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getSecondary () {
		return secondary;
	}   
	/**
	 * 属性secondary的Setter方法.属性名：协办单位
	 * 创建日期:
	 * @param newSecondary java.lang.String
	 */
	public void setSecondary (java.lang.String newSecondary ) {
	 	this.secondary = newSecondary;
	} 	  
	/**
	 * 属性pk_manager的Getter方法.属性名：活动负责人
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getPk_manager () {
		return pk_manager;
	}   
	/**
	 * 属性pk_manager的Setter方法.属性名：活动负责人
	 * 创建日期:
	 * @param newPk_manager java.lang.String
	 */
	public void setPk_manager (java.lang.String newPk_manager ) {
	 	this.pk_manager = newPk_manager;
	} 	  
	/**
	 * 属性phone的Getter方法.属性名：负责人电话
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getPhone () {
		return phone;
	}   
	/**
	 * 属性phone的Setter方法.属性名：负责人电话
	 * 创建日期:
	 * @param newPhone java.lang.String
	 */
	public void setPhone (java.lang.String newPhone ) {
	 	this.phone = newPhone;
	} 	  
	/**
	 * 属性plan_fee的Getter方法.属性名：计划费用总额
	 * 创建日期:
	 * @return nc.vo.pub.lang.UFDouble
	 */
	public nc.vo.pub.lang.UFDouble getPlan_fee () {
		return plan_fee;
	}   
	/**
	 * 属性plan_fee的Setter方法.属性名：计划费用总额
	 * 创建日期:
	 * @param newPlan_fee nc.vo.pub.lang.UFDouble
	 */
	public void setPlan_fee (nc.vo.pub.lang.UFDouble newPlan_fee ) {
	 	this.plan_fee = newPlan_fee;
	} 	  
	/**
	 * 属性fact_fee的Getter方法.属性名：实际费用总额
	 * 创建日期:
	 * @return nc.vo.pub.lang.UFDouble
	 */
	public nc.vo.pub.lang.UFDouble getFact_fee () {
		return fact_fee;
	}   
	/**
	 * 属性fact_fee的Setter方法.属性名：实际费用总额
	 * 创建日期:
	 * @param newFact_fee nc.vo.pub.lang.UFDouble
	 */
	public void setFact_fee (nc.vo.pub.lang.UFDouble newFact_fee ) {
	 	this.fact_fee = newFact_fee;
	} 	  
	/**
	 * 属性activestate的Getter方法.属性名：活动状态
	 * 创建日期:
	 * @return java.lang.Integer
	 */
	public java.lang.Integer getActivestate () {
		return activestate;
	}   
	/**
	 * 属性activestate的Setter方法.属性名：活动状态
	 * 创建日期:
	 * @param newActivestate java.lang.Integer
	 */
	public void setActivestate (java.lang.Integer newActivestate ) {
	 	this.activestate = newActivestate;
	} 	  
	/**
	 * 属性state_date的Getter方法.属性名：状态日期
	 * 创建日期:
	 * @return nc.vo.pub.lang.UFLiteralDate
	 */
	public nc.vo.pub.lang.UFLiteralDate getState_date () {
		return state_date;
	}   
	/**
	 * 属性state_date的Setter方法.属性名：状态日期
	 * 创建日期:
	 * @param newState_date nc.vo.pub.lang.UFLiteralDate
	 */
	public void setState_date (nc.vo.pub.lang.UFLiteralDate newState_date ) {
	 	this.state_date = newState_date;
	} 	  
	/**
	 * 属性remark的Getter方法.属性名：备注
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getRemark () {
		return remark;
	}   
	/**
	 * 属性remark的Setter方法.属性名：备注
	 * 创建日期:
	 * @param newRemark java.lang.String
	 */
	public void setRemark (java.lang.String newRemark ) {
	 	this.remark = newRemark;
	} 	  
	/**
	 * 属性creationtime的Getter方法.属性名：创建时间
	 * 创建日期:
	 * @return nc.vo.pub.lang.UFDateTime
	 */
	public nc.vo.pub.lang.UFDateTime getCreationtime () {
		return creationtime;
	}   
	/**
	 * 属性creationtime的Setter方法.属性名：创建时间
	 * 创建日期:
	 * @param newCreationtime nc.vo.pub.lang.UFDateTime
	 */
	public void setCreationtime (nc.vo.pub.lang.UFDateTime newCreationtime ) {
	 	this.creationtime = newCreationtime;
	} 	  
	/**
	 * 属性creator的Getter方法.属性名：创建人
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getCreator () {
		return creator;
	}   
	/**
	 * 属性creator的Setter方法.属性名：创建人
	 * 创建日期:
	 * @param newCreator java.lang.String
	 */
	public void setCreator (java.lang.String newCreator ) {
	 	this.creator = newCreator;
	} 	  
	/**
	 * 属性modifiedtime的Getter方法.属性名：最后修改时间
	 * 创建日期:
	 * @return nc.vo.pub.lang.UFDateTime
	 */
	public nc.vo.pub.lang.UFDateTime getModifiedtime () {
		return modifiedtime;
	}   
	/**
	 * 属性modifiedtime的Setter方法.属性名：最后修改时间
	 * 创建日期:
	 * @param newModifiedtime nc.vo.pub.lang.UFDateTime
	 */
	public void setModifiedtime (nc.vo.pub.lang.UFDateTime newModifiedtime ) {
	 	this.modifiedtime = newModifiedtime;
	} 	  
	/**
	 * 属性modifier的Getter方法.属性名：最后修改人
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getModifier () {
		return modifier;
	}   
	/**
	 * 属性modifier的Setter方法.属性名：最后修改人
	 * 创建日期:
	 * @param newModifier java.lang.String
	 */
	public void setModifier (java.lang.String newModifier ) {
	 	this.modifier = newModifier;
	} 	  
	/**
	 * 属性general_rules的Getter方法.属性名：活动简章
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getGeneral_rules () {
		return general_rules;
	}   
	/**
	 * 属性general_rules的Setter方法.属性名：活动简章
	 * 创建日期:
	 * @param newGeneral_rules java.lang.String
	 */
	public void setGeneral_rules (java.lang.String newGeneral_rules ) {
	 	this.general_rules = newGeneral_rules;
	} 	  
	/**
	 * 属性dr的Getter方法.属性名：dr
	 * 创建日期:
	 * @return java.lang.Integer
	 */
	public java.lang.Integer getDr () {
		return dr;
	}   
	/**
	 * 属性dr的Setter方法.属性名：dr
	 * 创建日期:
	 * @param newDr java.lang.Integer
	 */
	public void setDr (java.lang.Integer newDr ) {
	 	this.dr = newDr;
	} 	  
	/**
	 * 属性ts的Getter方法.属性名：ts
	 * 创建日期:
	 * @return nc.vo.pub.lang.UFDateTime
	 */
	public nc.vo.pub.lang.UFDateTime getTs () {
		return ts;
	}   
	/**
	 * 属性ts的Setter方法.属性名：ts
	 * 创建日期:
	 * @param newTs nc.vo.pub.lang.UFDateTime
	 */
	public void setTs (nc.vo.pub.lang.UFDateTime newTs ) {
	 	this.ts = newTs;
	} 	  
 
	/**
	  * <p>取得父VO主键字段.
	  * <p>
	  * 创建日期:
	  * @return java.lang.String
	  */
	public java.lang.String getParentPKFieldName() {
	    return null;
	}   
    
	/**
	  * <p>取得表主键.
	  * <p>
	  * 创建日期:
	  * @return java.lang.String
	  */
	public java.lang.String getPKFieldName() {
	  return "pk_active";
	}
    
	/**
	 * <p>返回表名称.
	 * <p>
	 * 创建日期:
	 * @return java.lang.String
	 */
	public java.lang.String getTableName() {
		return "rm_active";
	}    
	
	/**
	 * <p>返回表名称.
	 * <p>
	 * 创建日期:
	 * @return java.lang.String
	 */
	public static java.lang.String getDefaultTableName() {
		return "rm_active";
	}    
    
    /**
	  * 按照默认方式创建构造子.
	  *
	  * 创建日期:
	  */
     public ActiveVO() {
		super();	
	}    
	
	@nc.vo.annotation.MDEntityInfo(beanFullclassName =  "nc.vo.rm.active.ActiveVO" )
	public IVOMeta getMetaData() {
   		return null;
  	}
} 


