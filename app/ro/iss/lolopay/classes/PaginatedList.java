package ro.iss.lolopay.classes;

import java.util.List;
import ro.iss.lolopay.models.classes.TableCollection;

public class PaginatedList {
  /** Number of current page */
  private Long page;

  /** Number of records in current page */
  private Long pageSize;

  /** Total number of pages */
  private Long totalPages;

  /** Total records of the executed query */
  private Long totalRecords;

  private List<? extends TableCollection> list;

  /** @return the page */
  public Long getPage() {

    return page;
  }

  /** @param page the page to set */
  public void setPage(Long page) {

    this.page = page;
  }

  /** @return the pageSize */
  public Long getPageSize() {

    return pageSize;
  }

  /** @param pageSize the pageSize to set */
  public void setPageSize(Long pageSize) {

    this.pageSize = pageSize;
  }

  /** @return the totalPages */
  public Long getTotalPages() {

    return totalPages;
  }

  /** @param totalPages the totalPages to set */
  public void setTotalPages(Long totalPages) {

    this.totalPages = totalPages;
  }

  /** @return the totalRecords */
  public Long getTotalRecords() {

    return totalRecords;
  }

  /** @param totalRecords the totalRecords to set */
  public void setTotalRecords(Long totalRecords) {

    this.totalRecords = totalRecords;
  }

  /** @return the list */
  public List<? extends TableCollection> getList() {

    return list;
  }

  /** @param list the list to set */
  public void setList(List<? extends TableCollection> list) {

    this.list = list;
  }
}
