package nc.impl.rm.interview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.hr.frame.persistence.SimpleDocServiceTemplate;
import nc.hr.utils.InSQLCreator;
import nc.hr.utils.MultiLangHelper;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.hr.utils.StringPiecer;
import nc.itf.hi.IPsndocQryService;
import nc.itf.hr.message.IHRMessageSend;
import nc.itf.rm.IEvaItemQueryService;
import nc.itf.rm.IInterviewQueryMaintain;
import nc.itf.rm.IInterviewQueryService;
import nc.itf.rm.IPublishQueryService;
import nc.itf.rm.IRMPsndocManageService;
import nc.itf.rm.IRMPsndocQueryService;
import nc.itf.trn.transmng.ITransmngManageService;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.hi.psndoc.PsndocAggVO;
import nc.vo.hi.psndoc.PsndocVO;
import nc.vo.hi.pub.RM2TRNLinkData;
import nc.vo.hr.message.HRBusiMessageVO;
import nc.vo.om.hrdept.HRDeptVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.rm.evaitem.AggEvaItemVO;
import nc.vo.rm.evaitem.EvaItemVO;
import nc.vo.rm.interview.AggInterviewPlanVO;
import nc.vo.rm.interview.AggInterviewVO;
import nc.vo.rm.interview.EvaluateItemVO;
import nc.vo.rm.interview.IVPlanStateEnum;
import nc.vo.rm.interview.IVPlanVO;
import nc.vo.rm.interview.InterviewPlanVO;
import nc.vo.rm.interview.InterviewStatusEnum;
import nc.vo.rm.interview.InterviewVO;
import nc.vo.rm.interview.IvReslutEnum;
import nc.vo.rm.job.AggRMJobVO;
import nc.vo.rm.job.InterviewerEnum;
import nc.vo.rm.job.RMJobSchemeVO;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.psndoc.common.RMApplyStatusEnum;
import nc.vo.rm.psndoc.common.ResumeSourceEnum;
import nc.vo.rm.publish.PublishJobVO;
import nc.vo.sm.UserVO;
import nc.vo.util.BDVersionValidationUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class InterviewManageMaintianImpl implements nc.itf.rm.IInterviewManageMaintain
{
  private SimpleDocServiceTemplate serviceTemplate;
  private SimpleDocServiceTemplate serviceTemplateItem;
  private IRMPsndocManageService psndocManageService;
  
  public InterviewManageMaintianImpl() {}
  
  public AggInterviewVO insert(AggInterviewVO aggvo) throws BusinessException
  {
    aggvo = (AggInterviewVO)getServiceTemplate().insert(aggvo);
    updateAggPlans(aggvo);
    return orderByRoundNum(new AggInterviewVO[] { aggvo })[0];
  }
  



  private AggInterviewVO[] orderByRoundNum(AggInterviewVO... aggvos)
    throws BusinessException
  {
    if (ArrayUtils.isEmpty(aggvos))
      return aggvos;
    String[] pkArr = new String[aggvos.length];
    for (int i = 0; i < aggvos.length; i++) {
      pkArr[i] = aggvos[i].getInterviewVO().getPk_psndoc_job();
      InterviewPlanVO[] planvos = aggvos[i].getInterviewPlanVOs();
      if (!ArrayUtils.isEmpty(planvos))
      {
        for (int j = 0; j < planvos.length - 1; j++) {
          for (int k = j; k < planvos.length; k++)
            if (planvos[j].getRoundnum().intValue() > planvos[k].getRoundnum().intValue()) {
              InterviewPlanVO temp = planvos[j];
              planvos[j] = planvos[k];
              planvos[k] = temp;
            }
        }
      }
    }
    InSQLCreator isc = new InSQLCreator();
    try {
      String pks = isc.getInSQL(pkArr);
      String selSql = " pk_psndoc_job in(" + pks + ")";
      Collection<RMPsnJobVO> res = new BaseDAO().retrieveByClause(RMPsnJobVO.class, selSql);
      RMPsnJobVO[] jobVOs = (RMPsnJobVO[])res.toArray(new RMPsnJobVO[0]);
      if (ArrayUtils.isEmpty(jobVOs))
        return aggvos;
      for (AggInterviewVO aggvo : aggvos) {
        for (int i = 0; i < jobVOs.length; i++) {
          if (jobVOs[i].getPk_psndoc_job().equals(aggvo.getInterviewVO().getPk_psndoc_job())) {
            aggvo.getInterviewVO().setPk_reg_job(jobVOs[i].getPk_reg_job());
          }
        }
      }
    }
    finally {
      isc.clear();
    }
    
    return aggvos;
  }
  
  private void updateAggPlans(AggInterviewVO aggvo) throws BusinessException { if (aggvo == null)
      return;
    InterviewPlanVO[] planVOs = aggvo.getInterviewPlanVOs();
    if (ArrayUtils.isEmpty(planVOs))
      return;
    for (InterviewPlanVO plan : planVOs)
      if ((plan.getResult() == null) || ((plan.getResult().intValue() == IvReslutEnum.WAIT.toIntValue()) && ((plan.getIvstate() == null) || (plan.getIvstate().intValue() != IVPlanStateEnum.SAVED.toIntValue()))))
      {
        String pk_evatype = plan.getPk_evaitem();
        if (!StringUtils.isEmpty(pk_evatype))
        {

          BaseDAO dao = new BaseDAO();
          String deletecond = " pk_ivplan = '" + plan.getPk_ivplan() + "'";
          dao.deleteByClause(EvaluateItemVO.class, deletecond);
          IVPlanVO ivPlan = clone(plan);
          ivPlan.setIvstate(Integer.valueOf(IVPlanStateEnum.WAIT.toIntValue()));
          AggInterviewPlanVO aggPlanVO = new AggInterviewPlanVO();
          aggPlanVO.setParentVO(ivPlan);
          AggEvaItemVO[] evaItemVOs = ((IEvaItemQueryService)NCLocator.getInstance().lookup(IEvaItemQueryService.class)).queryByType(plan.getPk_evaitem(), plan.getPk_org());
          if (!ArrayUtils.isEmpty(evaItemVOs))
          {
            ivPlan.setStatus(1);
            List<EvaluateItemVO> evaVOList = new ArrayList();
            for (AggEvaItemVO aggItemVO : evaItemVOs) {
              EvaItemVO evaItemVO = (EvaItemVO)aggItemVO.getParentVO();
              EvaluateItemVO itemVO = new EvaluateItemVO();
              itemVO.setPk_item(evaItemVO.getPk_evaitem());
              itemVO.setPk_ivplan(ivPlan.getPk_ivplan());
              itemVO.setPk_org(plan.getPk_org());
              itemVO.setPk_group(plan.getPk_group());
              itemVO.setSummary(evaItemVO.getRemark());
              itemVO.setStatus(2);
              evaVOList.add(itemVO);
            }
            aggPlanVO.setChildrenVO((CircularlyAccessibleValueObject[])evaVOList.toArray(new EvaluateItemVO[0]));
            getServiceTemplateItem().update(aggPlanVO, true);
          }
        }
      } }
  
  private IVPlanVO clone(InterviewPlanVO plan) { if (plan == null)
      return null;
    IVPlanVO ivPlan = new IVPlanVO();
    String[] attNames = ivPlan.getAttributeNames();
    for (String attName : attNames) {
      ivPlan.setAttributeValue(attName, plan.getAttributeValue(attName));
    }
    return ivPlan;
  }
  
  public AggInterviewVO update(AggInterviewVO aggvo) throws BusinessException
  {
    aggvo = (AggInterviewVO)getServiceTemplate().update(aggvo, true);
    updateAggPlans(aggvo);
    return orderByRoundNum(new AggInterviewVO[] { aggvo })[0];
  }
  
  public SimpleDocServiceTemplate getServiceTemplate() {
    if (this.serviceTemplate == null) {
      this.serviceTemplate = new SimpleDocServiceTemplate("335039e1-7d1c-4fbe-a6ab-8ce4967abff7");
    }
    return this.serviceTemplate;
  }
  
  public void sendMessage(String pk_psnjob, AggInterviewVO vo)
    throws BusinessException
  {
    IHRMessageSend messageSendServer = (IHRMessageSend)NCLocator.getInstance().lookup(IHRMessageSend.class);
    HRBusiMessageVO messageVO = new HRBusiMessageVO();
    messageVO.setBillVO(vo.getInterviewVO());
    messageVO.setMsgrescode("602111");
    messageVO.setPkorgs(new String[] { vo.getInterviewVO().getPk_org() });
    PsndocAggVO psnVO = ((IPsndocQryService)NCLocator.getInstance().lookup(IPsndocQryService.class)).queryPsndocVOByPsnjobPk(pk_psnjob);
    
    if (psnVO == null)
      return;
    String pk_psndoc = psnVO.getParentVO().getPk_psndoc();
    HashMap<String, UserVO[]> userMap = ((IUserPubService)NCLocator.getInstance().lookup(IUserPubService.class)).batchQueryUserVOsByPsnDocID(new String[] { pk_psndoc }, null);
    Hashtable<String, Object> value = new Hashtable();
    value.put("interviewer", MultiLangHelper.getName(psnVO.getParentVO(), "name"));
    messageVO.setBusiVarValues(value);
    if (userMap.get(pk_psndoc) == null) {
      String email = psnVO.getParentVO().getEmail();
      String mobile = psnVO.getParentVO().getMobile();
      if (!StringUtils.isEmpty(email))
        messageVO.setReceiverEmails(new String[] { email });
      if (!StringUtils.isEmpty(mobile))
        messageVO.setReceiverMobiles(new String[] { mobile });
      messageSendServer.sendBuziMessage_RequiresNew(messageVO);
      return;
    }
    messageVO.setReceiverPkUsers(StringPiecer.getStrArray((SuperVO[])userMap.get(pk_psndoc), "cuserid"));
    messageSendServer.sendBuziMessage_RequiresNew(messageVO);
  }
  






































































































  public AggInterviewVO[] intoPsnlib(AggInterviewVO aggvo)
    throws BusinessException
  {
    if (aggvo == null)
      return null;
    String[] pk_psndocs = new String[1];
    pk_psndocs[0] = ((InterviewVO)aggvo.getParentVO()).getPk_psndoc();
    String pk_org = ((InterviewVO)aggvo.getParentVO()).getPk_org();
    IInterviewQueryMaintain queryMaintain = (IInterviewQueryMaintain)NCLocator.getInstance().lookup(IInterviewQueryMaintain.class);
    

    AggInterviewVO[] aggvos = orderByRoundNum(queryMaintain.queryInterviewByPsndoc(pk_psndocs[0]));
    
    if (!ArrayUtils.isEmpty(aggvos)) {
      for (int i = 0; i < aggvos.length; i++) {
        if ((InterviewStatusEnum.INTERVIEW.toIntValue() == aggvos[i].getInterviewVO().getInterviewstate().intValue()) && (aggvos[i].getInterviewVO().getIsuse() != null) && (UFBoolean.TRUE.equals(aggvos[i].getInterviewVO().getIsuse())))
        {
          throw new BusinessException(ResHelper.getString("6021interview", "06021interview0081")); }
        aggvos[i].getInterviewVO().setIsuse(UFBoolean.FALSE);
        aggvo = (AggInterviewVO)getServiceTemplate().update(aggvos[i], true);
      }
    }
    getIRMPsndocManageService().toPsnlib(pk_org, pk_psndocs, RMApplyStatusEnum.INTERVIEW.toIntValue());
    
    return aggvos;
  }
  
  public AggInterviewVO startInterview(AggInterviewVO aggvo)
    throws BusinessException
  {
    if (aggvo == null) {
      return null;
    }
    AggInterviewVO[] aggvos = getAggvo(aggvo, aggvo.getInterviewVO().getPk_psndoc());
    
    if (!ArrayUtils.isEmpty(aggvos)) {
      for (AggInterviewVO avo : aggvos) {
        if (((avo.getInterviewVO().getInterviewstate().intValue() == InterviewStatusEnum.INTERVIEW.toIntValue()) || (avo.getInterviewVO().getInterviewstate().intValue() == InterviewStatusEnum.PASS.toIntValue())) && (avo.getInterviewVO().getIsuse().equals(UFBoolean.TRUE)) && (!avo.getInterviewVO().getPk_interview().equals(aggvo.getInterviewVO().getPk_interview())))
        {


          throw new BusinessException(ResHelper.getString("6021interview", "06021interview0069"));
        }
      }
    }
    try
    {
      ((IRMPsndocQueryService)NCLocator.getInstance().lookup(IRMPsndocQueryService.class)).validateApplyStatus(new String[] { aggvo.getInterviewVO().getPk_psndoc() });
    } catch (BusinessException e) {
      throw new BusinessException(e.getMessage() + "," + ResHelper.getString("6021interview", "06021interview0082"));
    }
    
    ((InterviewVO)aggvo.getParentVO()).setInterviewstate(Integer.valueOf(InterviewStatusEnum.INTERVIEW.toIntValue()));
    //将面试轮次去掉
//    InterviewPlanVO[] ivPlans = aggvo.getInterviewPlanVOs();
//    if (ArrayUtils.isEmpty(ivPlans)) {
//      throw new BusinessException(ResHelper.getString("6021interview", "06021interview0064"));
//    }
//    
//    InterviewPlanVO resultPlan = null;
//    for (InterviewPlanVO ivPlan : ivPlans) {
//      if (1 == ivPlan.getRoundnum().intValue()) {
//        ivPlan.setBegindate(PubEnv.getServerLiteralDate());
//        ivPlan.setResult(Integer.valueOf(IvReslutEnum.WAIT.toIntValue()));
//        ivPlan.setIvstate(Integer.valueOf(IVPlanStateEnum.WAIT.toIntValue()));
//        resultPlan = ivPlan;
//        break;
//      }
//    }
//    if (resultPlan != null) {
//      new BaseDAO().updateVO(resultPlan, new String[] { "begindate", "result", "ivstate" });
//      try
//      {
//        sendMessage(resultPlan.getInterviewer(), aggvo);
//      } catch (Exception e) {
//        Logger.error(e.getMessage());
//      }
//    }
    

    aggvo = orderByRoundNum(new AggInterviewVO[] { (AggInterviewVO)getServiceTemplate().update(aggvo, true) })[0];
    return aggvo;
  }
  
  public AggInterviewVO[] getAggvo(AggInterviewVO aggvo, String pk_psndoc) throws BusinessException
  {
    AggInterviewVO[] aggvos = ((IInterviewQueryService)NCLocator.getInstance().lookup(IInterviewQueryService.class)).queryInterviewByPsndoc(pk_psndoc);
    

    return orderByRoundNum(aggvos);
  }
  
  public AggInterviewVO stopInterview(AggInterviewVO aggvo)
    throws BusinessException
  {
    if (aggvo == null)
      return null;
    aggvo.getInterviewVO().setInterviewstate(Integer.valueOf(InterviewStatusEnum.FAIL.toIntValue()));
    
    InterviewPlanVO[] ivPlans = aggvo.getInterviewPlanVOs();
    if (!ArrayUtils.isEmpty(ivPlans)) {
      InterviewPlanVO result = null;
      for (InterviewPlanVO ivPlan : ivPlans) {
        if ((IvReslutEnum.WAIT.toIntValue() == ivPlan.getResult().intValue()) || (IVPlanStateEnum.SAVED.toIntValue() == ivPlan.getIvstate().intValue())) {
          BDVersionValidationUtil.validateSuperVO(new SuperVO[] { ivPlan });
          ivPlan.setEnddate(PubEnv.getServerLiteralDate());
          ivPlan.setResult(Integer.valueOf(IvReslutEnum.FAIL.toIntValue()));
          ivPlan.setIvstate(Integer.valueOf(IVPlanStateEnum.SUBMIT.toIntValue()));
          ivPlan.setTs(PubEnv.getServerTime());
          result = ivPlan;
          break;
        }
      }
      if (result != null) {
        new BaseDAO().updateVO(result, new String[] { "enddate", "result", "ts" });
      }
    }
    aggvo = (AggInterviewVO)getServiceTemplate().update(aggvo, true);
    return orderByRoundNum(new AggInterviewVO[] { aggvo })[0];
  }
  

  private IRMPsndocManageService getIRMPsndocManageService()
  {
    if (this.psndocManageService == null) {
      this.psndocManageService = ((IRMPsndocManageService)NCLocator.getInstance().lookup(IRMPsndocManageService.class));
    }
    
    return this.psndocManageService;
  }
  
  public AggInterviewVO changeJob(AggInterviewVO aggvo, String pk_reg_dept, String pk_reg_org, String pk_job)
    throws BusinessException
  {
    if (aggvo == null) {
      return null;
    }
    AggRMJobVO jobVO = ((IPublishQueryService)NCLocator.getInstance().lookup(IPublishQueryService.class)).queryJobByPk(pk_job);
    
    AggInterviewVO aggivVO = clone(aggvo, pk_reg_dept, pk_reg_org, pk_job);
    RMJobSchemeVO[] schemevos = (RMJobSchemeVO[])jobVO.getTableVO("sub_scheme");
    if (ArrayUtils.isEmpty(schemevos))
    {
      aggvo.getInterviewVO().setIsuse(UFBoolean.FALSE);
      getServiceTemplate().update(aggvo, true);
      
      ((IRMPsndocManageService)NCLocator.getInstance().lookup(IRMPsndocManageService.class)).unUseJobsBypks(new String[] { aggvo.getInterviewVO().getPk_psndoc_job() });
      return insert(aggivVO);
    }
    
    InterviewPlanVO[] planvos = new InterviewPlanVO[schemevos.length];
    for (int i = 0; i < schemevos.length; i++) {
      InterviewPlanVO planvo = new InterviewPlanVO();
      planvo.setPk_org(schemevos[i].getPk_org());
      planvo.setPk_group(PubEnv.getPk_group());
      planvo.setRoundnum(schemevos[i].getRoundnum());
      planvo.setInterviewer(schemevos[i].getInterviewer());
      planvo.setViewertype(schemevos[i].getViewertype());
      planvo.setPk_viewer_dept(schemevos[i].getPk_viewer_dept());
      if (InterviewerEnum.DEPTPRINCIPAL.toIntValue() == schemevos[i].getViewertype().intValue()) {
        HRDeptVO deptVO = ((IInterviewQueryMaintain)NCLocator.getInstance().lookup(IInterviewQueryMaintain.class)).queryHRDeptVOByPK(pk_reg_dept);
        

        planvo.setPk_viewer_dept(pk_reg_dept);
        if ((deptVO != null) && (!StringUtils.isEmpty(deptVO.getPrincipal()))) {
          PsnJobVO psnJobVO = ((IInterviewQueryMaintain)NCLocator.getInstance().lookup(IInterviewQueryMaintain.class)).queryPsnJobVO(deptVO.getPrincipal(), pk_reg_dept);
          

          if (psnJobVO != null) {
            planvo.setInterviewer(psnJobVO.getPk_psnjob());
          }
        }
      } else if (InterviewerEnum.DEPTSUPERPRINCIPAL.toIntValue() == schemevos[i].getViewertype().intValue()) {
        HRDeptVO deptVO = ((IInterviewQueryMaintain)NCLocator.getInstance().lookup(IInterviewQueryMaintain.class)).queryFatherHRDeptVOByPK(pk_reg_dept);
        


        if ((deptVO != null) && (!StringUtils.isEmpty(deptVO.getPrincipal()))) {
          planvo.setPk_viewer_dept(deptVO.getPk_dept());
          PsnJobVO psnJobVO = ((IInterviewQueryMaintain)NCLocator.getInstance().lookup(IInterviewQueryMaintain.class)).queryPsnJobVO(deptVO.getPrincipal(), pk_reg_dept);
          

          if (psnJobVO != null) {
            planvo.setInterviewer(psnJobVO.getPk_psnjob());
          }
        }
      }
      planvo.setPk_evaitem(schemevos[i].getPk_evatype());
      planvos[i] = planvo;
      planvos[i].setStatus(2);
    }
    aggivVO.setChildrenVO(planvos);
    aggvo.getInterviewVO().setIsuse(UFBoolean.FALSE);
    getServiceTemplate().update(aggvo, true);
    aggivVO = insert(aggivVO);
    updateAggPlans(aggivVO);
    
    ((IRMPsndocManageService)NCLocator.getInstance().lookup(IRMPsndocManageService.class)).unUseJobsBypks(new String[] { aggvo.getInterviewVO().getPk_psndoc_job() });
    return aggivVO;
  }
  
  private AggInterviewVO clone(AggInterviewVO vo, String pk_reg_dept, String pk_reg_org, String pk_job) throws BusinessException
  {
    AggInterviewVO aggivVO = new AggInterviewVO();
    InterviewVO ivVO = vo.getInterviewVO();
    InterviewVO interviewVO = new InterviewVO();
    interviewVO.setPk_group(PubEnv.getPk_group());
    interviewVO.setPk_org(ivVO.getPk_org());
    interviewVO.setPk_psndoc(ivVO.getPk_psndoc());
    interviewVO.setPk_reg_dept(pk_reg_dept);
    interviewVO.setPk_reg_org(pk_reg_org);
    
    String pk_psndoc_job = ((IRMPsndocManageService)NCLocator.getInstance().lookup(IRMPsndocManageService.class)).saveApplyJob(createJobVO(vo, pk_reg_dept, pk_reg_org, pk_job));
    
    interviewVO.setPk_psndoc_job(pk_psndoc_job);
    interviewVO.setInterviewstate((Integer)InterviewStatusEnum.WAIT.value());
    
    interviewVO.setIsuse(UFBoolean.TRUE);
    interviewVO.setIsoffersended(UFBoolean.FALSE);
    aggivVO.setParentVO(interviewVO);
    

    return orderByRoundNum(new AggInterviewVO[] { aggivVO })[0];
  }
  

  private RMPsnJobVO createJobVO(AggInterviewVO vo, String pk_reg_dept, String pk_reg_org, String pk_job)
    throws BusinessException
  {
    RMPsnJobVO newJobVO = new RMPsnJobVO();
    newJobVO.setPk_group(vo.getInterviewVO().getPk_group());
    newJobVO.setPk_org(vo.getInterviewVO().getPk_org());
    newJobVO.setPk_psndoc(vo.getInterviewVO().getPk_psndoc());
    newJobVO.setPk_reg_dept(pk_reg_dept);
    newJobVO.setPk_reg_org(pk_reg_org);
    newJobVO.setPk_reg_job(pk_job);
    PublishJobVO publish = (PublishJobVO)new BaseDAO().retrieveByPK(PublishJobVO.class, pk_job);
    newJobVO.setPk_active(publish.getPk_activity());
    newJobVO.setPk_channel(publish.getPk_channel());
    newJobVO.setRecordnum(Integer.valueOf(0));
    newJobVO.setIspsnlib(UFBoolean.FALSE);
    newJobVO.setIsuse(UFBoolean.TRUE);
    newJobVO.setSourcetype(Integer.valueOf(ResumeSourceEnum.MANUAL.toIntValue()));
    newJobVO.setReg_date(PubEnv.getServerLiteralDate());
    newJobVO.setApplystatus(Integer.valueOf(RMApplyStatusEnum.INTERVIEW.toIntValue()));
    newJobVO.setApplystatusdate(PubEnv.getServerLiteralDate());
    newJobVO.setStatus(2);
    return newJobVO;
  }
  
  public AggInterviewVO sendMessage(AggInterviewVO aggvo)
    throws BusinessException
  {
    if (aggvo == null)
      return null;
    aggvo.getInterviewVO().setIsoffersended(UFBoolean.TRUE);
    aggvo = (AggInterviewVO)getServiceTemplate().update(aggvo, true);
    return orderByRoundNum(new AggInterviewVO[] { aggvo })[0];
  }
  
  public SimpleDocServiceTemplate getServiceTemplateItem() { if (this.serviceTemplateItem == null) {
      this.serviceTemplateItem = new SimpleDocServiceTemplate("1b145aa1-cfb3-4467-99f3-95f25f26659d");
    }
    return this.serviceTemplateItem;
  }
  
  public AggInterviewVO reqTrans(AggInterviewVO aggvo, RM2TRNLinkData data)
    throws BusinessException
  {
    if ((aggvo == null) || (data == null))
      return null;
    ((ITransmngManageService)NCLocator.getInstance().lookup(ITransmngManageService.class)).createCrossInBill4RM(data);
    aggvo.getInterviewVO().setIsuse(UFBoolean.FALSE);
    AggRMPsndocVO aggPsnVO = ((IRMPsndocQueryService)NCLocator.getInstance().lookup(IRMPsndocQueryService.class)).queryByPK(aggvo.getInterviewVO().getPk_psndoc());
    
    RMPsnJobVO[] psnJobVOs = (RMPsnJobVO[])aggPsnVO.getTableVO(RMPsnJobVO.getDefaultTableName());
    List<String> unUsePk = new ArrayList();
    for (RMPsnJobVO psnJobVO : psnJobVOs) {
      if (!psnJobVO.getPk_psndoc_job().equals(psnJobVO.getPk_psndoc_job()))
      {
        unUsePk.add(psnJobVO.getPk_psndoc_job()); }
    }
    if (!org.apache.commons.collections.CollectionUtils.isEmpty(unUsePk))
      ((IRMPsndocManageService)NCLocator.getInstance().lookup(IRMPsndocManageService.class)).unUseJobsBypks((String[])unUsePk.toArray(new String[0]));
    AggInterviewVO[] aggvos = queryBypsndocs(new String[] { aggvo.getInterviewVO().getPk_psndoc() });
    InSQLCreator isc = new InSQLCreator();
    try {
      ((IRMPsndocManageService)NCLocator.getInstance().lookup(IRMPsndocManageService.class)).updateApplyStatusByJobPks(RMApplyStatusEnum.INDOC.toIntValue(), RMApplyStatusEnum.INTERVIEW.toIntValue(), new String[] { aggvo.getInterviewVO().getPk_psndoc_job() });
    }
    finally {
      isc.clear();
    }
    if (ArrayUtils.isEmpty(aggvos))
      return null;
    for (AggInterviewVO agvo : aggvos) {
      agvo.getInterviewVO().setIsuse(UFBoolean.FALSE);
      getServiceTemplate().update(aggvo, true);
    }
    return orderByRoundNum(new AggInterviewVO[] { aggvo })[0];
  }
  
  private AggInterviewVO[] queryBypsndocs(String[] pk_psndocs) throws BusinessException
  {
    if (ArrayUtils.isEmpty(pk_psndocs))
      return null;
    InSQLCreator sqlCreator = new InSQLCreator();
    String inSQL = sqlCreator.getInSQL(pk_psndocs);
    String sql = "pk_psndoc in (" + inSQL + ") and isuse = 'Y' and isnull(ishire, 'N') = 'N'";
    sqlCreator.clear();
    return orderByRoundNum((AggInterviewVO[])getServiceTemplate().queryByCondition(AggInterviewVO.class, sql));
  }
}
