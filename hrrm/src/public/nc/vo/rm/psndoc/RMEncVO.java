package nc.vo.rm.psndoc;

import nc.vo.pub.IVOMeta;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.pubapp.pattern.model.meta.entity.vo.VOMetaFactory;











public class RMEncVO
  extends SuperVO
{
  private String pk_psndoc;
  private String pk_psndoc_enc;
  private String pk_group;
  private String pk_org;
  private String vencourtype;
  private String vencourunit;
  private String vencourmeas;
  private UFLiteralDate vencourdate;
  private String vencourmatter;
  private Integer recordnum;
  private Integer dr = Integer.valueOf(0);
  
  private UFDateTime ts;
  
  public static final String PK_PSNDOC = "pk_psndoc";
  
  public static final String PK_PSNDOC_ENC = "pk_psndoc_enc";
  
  public static final String PK_GROUP = "pk_group";
  
  public static final String PK_ORG = "pk_org";
  public static final String VENCOURTYPE = "vencourtype";
  public static final String VENCOURUNIT = "vencourunit";
  public static final String VENCOURMEAS = "vencourmeas";
  public static final String VENCOURDATE = "vencourdate";
  public static final String VENCOURMATTER = "vencourmatter";
  public static final String RECORDNUM = "recordnum";
  
  
  private String encourrank ;
  
 
  
  public String getEncourrank() {
	return this.encourrank;
}


public void setEncourrank(String newEncourrank) {
	this.encourrank = newEncourrank;
}




public String getPk_psndoc()
  {
    return this.pk_psndoc;
  }
  



  public void setPk_psndoc(String newPk_psndoc)
  {
    this.pk_psndoc = newPk_psndoc;
  }
  



  public String getPk_psndoc_enc()
  {
    return this.pk_psndoc_enc;
  }
  



  public void setPk_psndoc_enc(String newPk_psndoc_enc)
  {
    this.pk_psndoc_enc = newPk_psndoc_enc;
  }
  

  public String getPk_group()
  {
    return this.pk_group;
  }
  
  public void setPk_group(String newPk_group)
  {
    this.pk_group = newPk_group;
  }
  



  public String getPk_org()
  {
    return this.pk_org;
  }
  



  public void setPk_org(String newPk_org)
  {
    this.pk_org = newPk_org;
  }
  



  public String getVencourtype()
  {
    return this.vencourtype;
  }
  



  public void setVencourtype(String newVencourtype)
  {
    this.vencourtype = newVencourtype;
  }
  



  public String getVencourunit()
  {
    return this.vencourunit;
  }
  



  public void setVencourunit(String newVencourunit)
  {
    this.vencourunit = newVencourunit;
  }
  



  public String getVencourmeas()
  {
    return this.vencourmeas;
  }
  



  public void setVencourmeas(String newVencourmeas)
  {
    this.vencourmeas = newVencourmeas;
  }
  



  public UFLiteralDate getVencourdate()
  {
    return this.vencourdate;
  }
  



  public void setVencourdate(UFLiteralDate newVencourdate)
  {
    this.vencourdate = newVencourdate;
  }
  



  public String getVencourmatter()
  {
    return this.vencourmatter;
  }
  



  public void setVencourmatter(String newVencourmatter)
  {
    this.vencourmatter = newVencourmatter;
  }
  



  public Integer getRecordnum()
  {
    return this.recordnum;
  }
  



  public void setRecordnum(Integer newRecordnum)
  {
    this.recordnum = newRecordnum;
  }
  



  public Integer getDr()
  {
    return this.dr;
  }
  



  public void setDr(Integer newDr)
  {
    this.dr = newDr;
  }
  



  public UFDateTime getTs()
  {
    return this.ts;
  }
  



  public void setTs(UFDateTime newTs)
  {
    this.ts = newTs;
  }
  





  public String getParentPKFieldName()
  {
    return "pk_psndoc";
  }
  





  public String getPKFieldName()
  {
    return "pk_psndoc_enc";
  }
  





  public String getTableName()
  {
    return "rm_psndoc_enc";
  }
  





  public static String getDefaultTableName()
  {
    return "rm_psndoc_enc";
  }
  



  public RMEncVO() {}
  



  public IVOMeta getMetaData()
  {
    return VOMetaFactory.getInstance().getVOMeta("hrrm." + getTableName());
  }
}
