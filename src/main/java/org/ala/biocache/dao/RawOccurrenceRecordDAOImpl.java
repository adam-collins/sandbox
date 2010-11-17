package org.ala.biocache.dao;
/***************************************************************************
 * Copyright (C) 2005 Global Biodiversity Information Facility Secretariat.
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.ala.biocache.model.RawOccurrenceRecord;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * A pure jdbc implementation
 * 
 * @author trobertson
 */
public class RawOccurrenceRecordDAOImpl extends JdbcDaoSupport implements RawOccurrenceRecordDAO {
        
        protected static final String PROC_SQL = "call addCitizenScienceRecord(?,?,?,?,?,?,?,?,?,?,"
                                                                            + "?,?,?,?,?,?,?,?,?,?,"
                                                                            + "?,?,?,?,?,?,?,?,?,?,"
                                                                            + "?,?,?,?,?,?,?,?,?,?,"
                                                                            + "?,?,?,?,?,?,?,?,?,?,?,?)";

	/**
	 * The create SQL
	 */
	protected static final String CREATE_SQL = "insert into raw_occurrence_record("
			+ "data_provider_id,"
			+ "data_resource_id,"
			+ "resource_access_point_id,"
			+ "institution_code,"
			+ "collection_code,"
			+ "catalogue_number,"
			+ "scientific_name,"
			+ "author,"
			+ "rank,"
			+ "vernacular_name,"
			+ "kingdom,"
			+ "phylum,"
			+ "class,"
			+ "order_rank,"
			+ "family,"
			+ "genus,"
			+ "species,"
			+ "subspecies,"
			+ "latitude,"
			+ "longitude,"
			+ "lat_long_precision,"
			+ "min_altitude,"
			+ "max_altitude,"
			+ "altitude_precision,"
			+ "min_depth,"
			+ "max_depth,"
			+ "depth_precision,"
			+ "continent_ocean,"
			+ "country,"
			+ "state_province,"
			+ "county,"
			+ "collector_name,"
			+ "locality,"
			+ "year,"
			+ "month,"
			+ "day,"
			+ "event_time,"
			+ "basis_of_record,"
			+ "identifier_name,"
			+ "identification_date,"
			+ "unit_qualifier,"
			+ "created,"
			+ "modified, "
			+ "taxon_concept_guid,"
		    + "user_id," 
		    + "occurrence_remarks," 
		    + "location_remarks,"
		    + "individual_count,"
		    + "citation,"		    
		    + "geodetic_datum,"
		    + "generalised_metres"
			+ ") values (" 
			+ "?,?,?,?,?,?,?,?,?,?,"
			+ "?,?,?,?,?,?,?,?,?,?,"
			+ "?,?,?,?,?,?,?,?,?,?," 
			+ "?,?,?,?,?,?,?,?,?,?," 
			+ "?,?,?,?,?,?,?,?,?,?,?)";

	protected String citation;
	protected String geodeticDatum;
	protected Integer generalisedInMetres;	
	
	/**
	 * The update SQL
	 */
	protected static final String UPDATE_SQL = "update raw_occurrence_record set "
			+ "data_provider_id=?,"
			+ "data_resource_id=?,"
			+ "resource_access_point_id=?,"
			+ "institution_code=?,"
			+ "collection_code=?,"
			+ "catalogue_number=?,"
			+ "scientific_name=?,"
			+ "author=?,"
			+ "rank=?,"
			+ "vernacular_name=?,"
			+ "kingdom=?,"
			+ "phylum=?,"
			+ "class=?,"
			+ "order_rank=?,"
			+ "family=?,"
			+ "genus=?,"
			+ "species=?,"
			+ "subspecies=?,"
			+ "latitude=?,"
			+ "longitude=?,"
			+ "lat_long_precision=?,"
			+ "min_altitude=?,"
			+ "max_altitude=?,"
			+ "altitude_precision=?,"
			+ "min_depth=?,"
			+ "max_depth=?,"
			+ "depth_precision=?,"
			+ "continent_ocean=?,"
			+ "country=?,"
			+ "state_province=?,"
			+ "county=?,"
			+ "collector_name=?,"
			+ "locality=?,"
			+ "year=?,"
			+ "month=?,"
			+ "day=?,"
			+ "event_time=?,"
			+ "basis_of_record=?,"
			+ "identifier_name=?,"
			+ "identification_date=?,"
			+ "unit_qualifier=?,"
			+ "modified=?, "
			+ "taxon_concept_guid=?, "
			+ "user_id=?, "
			+ "occurrence_remarks=?, "
			+ "location_remarks=?, "
			+ "individual_count=?, "
		    + "citation=?,"		    
		    + "geodetic_datum=?,"
		    + "generalised_metres=?"
			+ "where id=?";
	
	/**
	 * The query by "business logic" unique Note that further criteria are added
	 * in the methods!
	 */
	protected static final String QUERY_UNIQUE_SQL = "select ror.id, data_provider_id,data_resource_id,resource_access_point_id,institution_code,collection_code,catalogue_number, "
			+ "scientific_name,author,rank,kingdom,phylum,class,order_rank,family,"
			+ "genus,species,subspecies,latitude,longitude,lat_long_precision,min_altitude,max_altitude,altitude_precision,min_depth,max_depth,depth_precision,continent_ocean,country,state_province,county,collector_name,"
			+ "locality,year,month,day,event_time,basis_of_record,identifier_name,identification_date,unit_qualifier,created,modified,deleted,taxon_concept_guid,user_id,vernacular_name," 
			+ "occurrence_remarks,location_remarks,individual_count,citation,geodetic_datum,generalised_metres "
			+ "from raw_occurrence_record ror " + "where data_resource_id=? ";

	/**
	 * The query by id
	 */
	protected static final String QUERY_ID_SQL = "select ror.id, data_provider_id,data_resource_id,resource_access_point_id,institution_code,collection_code,catalogue_number, "
			+ "scientific_name,author,rank,kingdom,phylum,class,order_rank,family,"
			+ "genus,species,subspecies,latitude,longitude,lat_long_precision,min_altitude,max_altitude,altitude_precision,min_depth,max_depth,depth_precision,continent_ocean,country,state_province,county,collector_name,"
			+ "locality,year,month,day,event_time,basis_of_record,identifier_name,identification_date,unit_qualifier,created,modified,deleted,taxon_concept_guid,user_id,vernacular_name,  "
			+ "occurrence_remarks, location_remarks,individual_count,citation,geodetic_datum,generalised_metres "
			+ "from raw_occurrence_record ror " + "where id=? ";

	/**
	 * The query by "business logic" unique Note that further criteria are added
	 * in the methods!
	 */
	protected static final String QUERY_MODIFIED_SINCE = "select ror.id, ror.data_provider_id,ror.data_resource_id,ror.resource_access_point_id,ror.institution_code,ror.collection_code,ror.catalogue_number, "
			+ "ror.scientific_name,ror.author,ror.rank,ror.kingdom,ror.phylum,ror.class,ror.order_rank,ror.family,"
			+ "ror.genus,ror.species,ror.subspecies,ror.latitude,ror.longitude,ror.lat_long_precision,ror.min_altitude,ror.max_altitude,ror.altitude_precision,ror.min_depth,ror.max_depth,ror.depth_precision,ror.continent_ocean,ror.country,ror.state_province,ror.county,ror.collector_name,"
			+ "ror.locality,ror.year,ror.month,ror.day,ror.event_time,ror.basis_of_record,ror.identifier_name,ror.identification_date,ror.unit_qualifier,ror.created,ror.modified,ror.deleted,ror.taxon_concept_guid,ror.user_id,ror.vernacular_name,  "
			+ "ror.occurrence_remarks,ror.location_remarks,individual_count,citation,geodetic_datum,generalised_metres "
			+ "from raw_occurrence_record ror "
			+ "where ror.data_resource_id=? and "
			+ "ror.modified>? and "
			+ "ror.id>? " + "order by ror.id " + "limit ?";

	/**
	 * The query for the classification
	 */
	protected static final String QUERY_RAW_TAXONOMY = "select kingdom, phylum, class, order_rank, family, genus, scientific_name "
			+ "from raw_occurrence_record ror "
			+ "where ror.data_resource_id=? "
			+ "group by kingdom, phylum, class, order_rank, family, genus, scientific_name";

	protected static final String QUERY_DATA_RESOURCE = "select distinct data_resource_id from raw_occurrence_record where resource_access_point_id=? order by 1";

    /**
     * The delete by id
     */
    protected static final String DELETE_SQL = "DELETE FROM raw_occurrence_record WHERE id=?";

	/**
	 * RawOccurrenceRecord row mapper
	 */
	protected RawOccurrenceRecordRowMapper rawOccurrenceRecordRowMapper = new RawOccurrenceRecordRowMapper();
	protected LongRowMapper longRowMapper = new LongRowMapper();

	protected class LongRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int arg1) throws SQLException {
			return new Long(rs.getLong(1));
		}
	}

	/**
	 * Utility to create a RawOccurrenceRecord for a row
	 * 
	 * @author trobertson
	 */
	protected class RawOccurrenceRecordRowMapper implements RowMapper {
		/**
		 * The factory
		 */
		public RawOccurrenceRecord mapRow(ResultSet rs, int rowNumber)
				throws SQLException {
			return new RawOccurrenceRecord(rs.getLong("id"),
					rs.getLong("data_provider_id"),
					rs.getLong("data_resource_id"),
					rs.getLong("resource_access_point_id"),
					rs.getString("institution_code"),
					rs.getString("collection_code"),
					rs.getString("catalogue_number"),
					rs.getString("scientific_name"), rs.getString("author"),
					rs.getString("rank"), 
					rs.getString("vernacular_name"),
					rs.getString("kingdom"),
					rs.getString("phylum"), rs.getString("class"),
					rs.getString("order_rank"), rs.getString("family"),
					rs.getString("genus"), rs.getString("species"),
					rs.getString("subspecies"), rs.getString("latitude"),
					rs.getString("longitude"),
					rs.getString("lat_long_precision"),
					rs.getString("min_altitude"), rs.getString("max_altitude"),
					rs.getString("altitude_precision"),
					rs.getString("min_depth"), rs.getString("max_depth"),
					rs.getString("depth_precision"),
					rs.getString("continent_ocean"), rs.getString("country"),
					rs.getString("state_province"), rs.getString("county"),
					rs.getString("collector_name"), rs.getString("locality"),
					rs.getString("year"), rs.getString("month"),
					rs.getString("day"), rs.getString("event_time"), rs.getString("basis_of_record"),
					rs.getString("identifier_name"),
					rs.getDate("identification_date"),
					rs.getString("unit_qualifier"), rs.getDate("created"),
					rs.getDate("modified"), rs.getDate("deleted"), 
					rs.getString("taxon_concept_guid"),
					rs.getString("user_id"),
					rs.getString("occurrence_remarks"),
					rs.getString("location_remarks"),
					(Integer) rs.getObject("individual_count"),
					rs.getString("citation"),
					rs.getString("geodetic_datum"),
					(Integer) rs.getObject("generalised_metres")					
			);
		}
	}
        /**
         * Creates a new raw occurrence record using the "addCitizenScienceRecord" stored procedure.
         *
         * Using this procedure instead of a direct insert will ensure that unique id's are being assigned.
         *
         * @param rawOccurrenceRecord
         * @return
         */
        public long create(final RawOccurrenceRecord rawOccurrenceRecord){
            java.util.List<SqlParameter> params = new java.util.ArrayList<SqlParameter>();
            params.add(new SqlOutParameter("o_id", Types.NUMERIC));
            java.util.Map<String, Object> results =getJdbcTemplate().call(new CallableStatementCreator(){
                Timestamp createTime = new Timestamp(System.currentTimeMillis());
            @Override
            public CallableStatement createCallableStatement(Connection con) throws SQLException {
                CallableStatement cs = con.prepareCall(PROC_SQL);
                cs.registerOutParameter(1, Types.NUMERIC);
                cs.setLong(2, rawOccurrenceRecord.getDataProviderId());
                cs.setLong(3, rawOccurrenceRecord.getDataResourceId());
                cs.setLong(4, rawOccurrenceRecord.getResourceAccessPointId());
                cs.setString(5, StringUtils.trimToNull(rawOccurrenceRecord.getInstitutionCode()));
                cs.setString(6, rawOccurrenceRecord.getCollectionCode());
                cs.setString(7, rawOccurrenceRecord.getCatalogueNumber());
                cs.setString(8, StringUtils.trimToNull(rawOccurrenceRecord.getScientificName()));
                cs.setString(9, StringUtils.trimToNull(rawOccurrenceRecord.getAuthor()));
                cs.setString(10, StringUtils.trimToNull(rawOccurrenceRecord.getRank()));
                cs.setString(11, StringUtils.trimToNull(rawOccurrenceRecord.getVernacularName()));
                cs.setString(12, StringUtils.trimToNull(rawOccurrenceRecord.getKingdom()));
                cs.setString(13, StringUtils.trimToNull(rawOccurrenceRecord.getPhylum()));
                cs.setString(14, StringUtils.trimToNull(rawOccurrenceRecord.getKlass()));
                cs.setString(15, StringUtils.trimToNull(rawOccurrenceRecord.getOrder()));
                cs.setString(16, StringUtils.trimToNull(rawOccurrenceRecord.getFamily()));
                cs.setString(17, StringUtils.trimToNull(rawOccurrenceRecord.getGenus()));
                cs.setString(18, StringUtils.trimToNull(rawOccurrenceRecord.getSpecies()));
                cs.setString(19, StringUtils.trimToNull(rawOccurrenceRecord.getSubspecies()));
                cs.setString(20, StringUtils.trimToNull(rawOccurrenceRecord.getLatitude()));
                cs.setString(21, StringUtils.trimToNull(rawOccurrenceRecord.getLongitude()));
                cs.setString(22, StringUtils.trimToNull(rawOccurrenceRecord.getLatLongPrecision()));
                cs.setString(23, StringUtils.trimToNull(rawOccurrenceRecord.getMinAltitude()));
                cs.setString(24, StringUtils.trimToNull(rawOccurrenceRecord.getMaxAltitude()));
                cs.setString(25, StringUtils.trimToNull(rawOccurrenceRecord.getAltitudePrecision()));
                cs.setString(26, StringUtils.trimToNull(rawOccurrenceRecord.getMinDepth()));
                cs.setString(27, StringUtils.trimToNull(rawOccurrenceRecord.getMaxDepth()));
                cs.setString(28, StringUtils.trimToNull(rawOccurrenceRecord.getDepthPrecision()));
                cs.setString(29, StringUtils.trimToNull(rawOccurrenceRecord.getContinentOrOcean()));
                cs.setString(30, StringUtils.trimToNull(rawOccurrenceRecord.getCountry()));
                cs.setString(31, StringUtils.trimToNull(rawOccurrenceRecord.getStateOrProvince()));
                cs.setString(32, StringUtils.trimToNull(rawOccurrenceRecord.getCounty()));
                cs.setString(33, StringUtils.trimToNull(rawOccurrenceRecord.getCollectorName()));
                cs.setString(34, StringUtils.trimToNull(rawOccurrenceRecord.getLocality()));
                cs.setString(35, StringUtils.trimToNull(rawOccurrenceRecord.getYear()));
                cs.setString(36, StringUtils.trimToNull(rawOccurrenceRecord.getMonth()));
                cs.setString(37, StringUtils.trimToNull(rawOccurrenceRecord.getDay()));
                cs.setString(38, StringUtils.trimToNull(rawOccurrenceRecord.getEventTime()));
                cs.setString(39, StringUtils.trimToNull(rawOccurrenceRecord.getBasisOfRecord()));
                cs.setString(40, StringUtils.trimToNull(rawOccurrenceRecord.getIdentifierName()));
                cs.setDate(41, createSQLDate(rawOccurrenceRecord.getDateIdentified()));
                cs.setString(42, StringUtils.trimToNull(rawOccurrenceRecord.getUnitQualifier()));
                cs.setTimestamp(43, createTime);
                cs.setTimestamp(44, createTime);
                cs.setString(45, rawOccurrenceRecord.getTaxonConceptGuid());
                cs.setString(46, rawOccurrenceRecord.getUserId());
                cs.setString(47, rawOccurrenceRecord.getOccurrenceRemarks());
                cs.setString(48, rawOccurrenceRecord.getLocationRemarks());
                cs.setObject(49, rawOccurrenceRecord.getIndividualCount());
                cs.setString(50, rawOccurrenceRecord.getCitation());
                cs.setString(51, rawOccurrenceRecord.getGeodeticDatum());
                cs.setObject(52, rawOccurrenceRecord.getGeneralisedInMetres());
                
                return cs;
            }
        }, params);

            //now get the id that was added
            Object value = results.get("o_id");
            if(value != null)
                return Long.parseLong(value.toString());
            return -1;
        }

	/**
         * OLD create that does not use the stored procedure
	 * @see org.gbif.portal.dao.RawOccurrenceRecordDAO#create(org.gbif.portal.model.RawOccurrenceRecord)
	 */
	/*public long create(final RawOccurrenceRecord rawOccurrenceRecord) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(new PreparedStatementCreator() {
			Timestamp createTime = new Timestamp(System.currentTimeMillis());

			public PreparedStatement createPreparedStatement(Connection conn)
					throws SQLException {
				PreparedStatement ps = conn.prepareStatement(RawOccurrenceRecordDAOImpl.CREATE_SQL, Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, rawOccurrenceRecord.getDataProviderId());
				ps.setLong(2, rawOccurrenceRecord.getDataResourceId());
				ps.setLong(3, rawOccurrenceRecord.getResourceAccessPointId());
				ps.setString(4, rawOccurrenceRecord.getInstitutionCode());
				ps.setString(5, rawOccurrenceRecord.getCollectionCode());
				ps.setString(6, rawOccurrenceRecord.getCatalogueNumber());
				ps.setString(7, StringUtils.trimToNull(rawOccurrenceRecord.getScientificName()));
				ps.setString(8, StringUtils.trimToNull(rawOccurrenceRecord.getAuthor()));
				ps.setString(9, StringUtils.trimToNull(rawOccurrenceRecord.getRank()));
				ps.setString(10, StringUtils.trimToNull(rawOccurrenceRecord.getVernacularName()));
				ps.setString(11, StringUtils.trimToNull(rawOccurrenceRecord.getKingdom()));
				ps.setString(12, StringUtils.trimToNull(rawOccurrenceRecord.getPhylum()));
				ps.setString(13, StringUtils.trimToNull(rawOccurrenceRecord.getKlass()));
				ps.setString(14, StringUtils.trimToNull(rawOccurrenceRecord.getOrder()));
				ps.setString(15, StringUtils.trimToNull(rawOccurrenceRecord.getFamily()));
				ps.setString(16, StringUtils.trimToNull(rawOccurrenceRecord.getGenus()));
				ps.setString(17, StringUtils.trimToNull(rawOccurrenceRecord.getSpecies()));
				ps.setString(18, StringUtils.trimToNull(rawOccurrenceRecord.getSubspecies()));
				ps.setString(19, StringUtils.trimToNull(rawOccurrenceRecord.getLatitude()));
				ps.setString(20, StringUtils.trimToNull(rawOccurrenceRecord.getLongitude()));
				ps.setString(21, StringUtils.trimToNull(rawOccurrenceRecord.getLatLongPrecision()));
				ps.setString(22, StringUtils.trimToNull(rawOccurrenceRecord.getMinAltitude()));
				ps.setString(23, StringUtils.trimToNull(rawOccurrenceRecord.getMaxAltitude()));
				ps.setString(24, StringUtils.trimToNull(rawOccurrenceRecord.getAltitudePrecision()));
				ps.setString(25, StringUtils.trimToNull(rawOccurrenceRecord.getMinDepth()));
				ps.setString(26, StringUtils.trimToNull(rawOccurrenceRecord.getMaxDepth()));
				ps.setString(27, StringUtils.trimToNull(rawOccurrenceRecord.getDepthPrecision()));
				ps.setString(28, StringUtils.trimToNull(rawOccurrenceRecord.getContinentOrOcean()));
				ps.setString(29, StringUtils.trimToNull(rawOccurrenceRecord.getCountry()));
				ps.setString(30, StringUtils.trimToNull(rawOccurrenceRecord.getStateOrProvince()));
				ps.setString(31, StringUtils.trimToNull(rawOccurrenceRecord.getCounty()));
				ps.setString(32, StringUtils.trimToNull(rawOccurrenceRecord.getCollectorName()));
				ps.setString(33, StringUtils.trimToNull(rawOccurrenceRecord.getLocality()));
				ps.setString(34, StringUtils.trimToNull(rawOccurrenceRecord.getYear()));
				ps.setString(35, StringUtils.trimToNull(rawOccurrenceRecord.getMonth()));
				ps.setString(36, StringUtils.trimToNull(rawOccurrenceRecord.getDay()));
				ps.setString(37, StringUtils.trimToNull(rawOccurrenceRecord.getEventTime()));
				ps.setString(38, StringUtils.trimToNull(rawOccurrenceRecord.getBasisOfRecord()));
				ps.setString(39, StringUtils.trimToNull(rawOccurrenceRecord.getIdentifierName()));
				ps.setDate(40, createSQLDate(rawOccurrenceRecord.getDateIdentified()));
				ps.setString(41, StringUtils.trimToNull(rawOccurrenceRecord.getUnitQualifier()));
				ps.setTimestamp(42, createTime);
				ps.setTimestamp(43, createTime);
				ps.setString(44, rawOccurrenceRecord.getTaxonConceptGuid());
				ps.setString(45, rawOccurrenceRecord.getUserId());
				ps.setString(46, rawOccurrenceRecord.getOccurrenceRemarks());
				ps.setString(47, rawOccurrenceRecord.getLocationRemarks());
				ps.setObject(48, rawOccurrenceRecord.getIndividualCount());
				ps.setString(49, rawOccurrenceRecord.getCitation());
				ps.setString(50, rawOccurrenceRecord.getGeodeticDatum());
				ps.setObject(51, rawOccurrenceRecord.getGeneralisedInMetres());
				return ps;
			}
		}, keyHolder);
		rawOccurrenceRecord.setId(keyHolder.getKey().longValue());
		return keyHolder.getKey().longValue();
	}*/

	/**
	 * @see org.gbif.portal.dao.RawOccurrenceRecordDAO#updateOrCreate(org.gbif.portal.model.RawOccurrenceRecord)
	 */
	public long updateOrCreate(final RawOccurrenceRecord rawOccurrenceRecord) {
		if (rawOccurrenceRecord.getId() <= 0) {
			return create(rawOccurrenceRecord);
		} else {
			getJdbcTemplate().update(new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection conn)
						throws SQLException {
					PreparedStatement ps = conn.prepareStatement(RawOccurrenceRecordDAOImpl.UPDATE_SQL);
					ps.setLong(1, rawOccurrenceRecord.getDataProviderId());
					ps.setLong(2, rawOccurrenceRecord.getDataResourceId());
					ps.setLong(3, rawOccurrenceRecord.getResourceAccessPointId());
					ps.setString(4, rawOccurrenceRecord.getInstitutionCode());
					ps.setString(5, rawOccurrenceRecord.getCollectionCode());
					ps.setString(6, rawOccurrenceRecord.getCatalogueNumber());
					ps.setString(7, StringUtils.trimToNull(rawOccurrenceRecord.getScientificName()));
					ps.setString(8, StringUtils.trimToNull(rawOccurrenceRecord.getVernacularName()));
					ps.setString(9, StringUtils.trimToNull(rawOccurrenceRecord.getAuthor()));
					ps.setString(10, StringUtils.trimToNull(rawOccurrenceRecord.getRank()));
					ps.setString(11, StringUtils.trimToNull(rawOccurrenceRecord.getKingdom()));
					ps.setString(12, StringUtils.trimToNull(rawOccurrenceRecord.getPhylum()));
					ps.setString(13, StringUtils.trimToNull(rawOccurrenceRecord.getKlass()));
					ps.setString(14, StringUtils.trimToNull(rawOccurrenceRecord.getOrder()));
					ps.setString(15, StringUtils.trimToNull(rawOccurrenceRecord.getFamily()));
					ps.setString(16, StringUtils.trimToNull(rawOccurrenceRecord.getGenus()));
					ps.setString(17, StringUtils.trimToNull(rawOccurrenceRecord.getSpecies()));
					ps.setString(18, StringUtils.trimToNull(rawOccurrenceRecord.getSubspecies()));
					ps.setString(19, StringUtils.trimToNull(rawOccurrenceRecord.getLatitude()));
					ps.setString(20, StringUtils.trimToNull(rawOccurrenceRecord.getLongitude()));
					ps.setString(21, StringUtils.trimToNull(rawOccurrenceRecord.getLatLongPrecision()));
					ps.setString(22, StringUtils.trimToNull(rawOccurrenceRecord.getMinAltitude()));
					ps.setString(23, StringUtils.trimToNull(rawOccurrenceRecord.getMaxAltitude()));
					ps.setString(24, StringUtils.trimToNull(rawOccurrenceRecord.getAltitudePrecision()));
					ps.setString(25, StringUtils.trimToNull(rawOccurrenceRecord.getMinDepth()));
					ps.setString(26, StringUtils.trimToNull(rawOccurrenceRecord.getMaxDepth()));
					ps.setString(27, StringUtils.trimToNull(rawOccurrenceRecord.getDepthPrecision()));
					ps.setString(28, StringUtils.trimToNull(rawOccurrenceRecord.getContinentOrOcean()));
					ps.setString(29, StringUtils.trimToNull(rawOccurrenceRecord.getCountry()));
					ps.setString(30, StringUtils.trimToNull(rawOccurrenceRecord.getStateOrProvince()));
					ps.setString(31, StringUtils.trimToNull(rawOccurrenceRecord.getCounty()));
					ps.setString(32, StringUtils.trimToNull(rawOccurrenceRecord.getCollectorName()));
					ps.setString(33, StringUtils.trimToNull(rawOccurrenceRecord.getLocality()));
					ps.setString(34, StringUtils.trimToNull(rawOccurrenceRecord.getYear()));
					ps.setString(35, StringUtils.trimToNull(rawOccurrenceRecord.getMonth()));
					ps.setString(36, StringUtils.trimToNull(rawOccurrenceRecord.getDay()));
					ps.setString(37, StringUtils.trimToNull(rawOccurrenceRecord.getEventTime()));
					ps.setString(38, StringUtils.trimToNull(rawOccurrenceRecord.getBasisOfRecord()));
					ps.setString(39, StringUtils.trimToNull(rawOccurrenceRecord.getIdentifierName()));
					ps.setDate(40, createSQLDate(rawOccurrenceRecord.getDateIdentified()));
					ps.setString(41, StringUtils.trimToNull(rawOccurrenceRecord.getUnitQualifier()));
					ps.setTimestamp(42, new Timestamp(System.currentTimeMillis()));
					ps.setString(43, rawOccurrenceRecord.getTaxonConceptGuid());
					ps.setString(44, rawOccurrenceRecord.getUserId());
					ps.setString(45, rawOccurrenceRecord.getOccurrenceRemarks());
					ps.setString(46, rawOccurrenceRecord.getLocationRemarks());
					ps.setObject(47, rawOccurrenceRecord.getIndividualCount());					
					ps.setString(48, rawOccurrenceRecord.getCitation());
					ps.setString(49, rawOccurrenceRecord.getGeodeticDatum());
					ps.setObject(50, rawOccurrenceRecord.getGeneralisedInMetres());
					ps.setLong(51, rawOccurrenceRecord.getId());
					return ps;
				}
			});
			return rawOccurrenceRecord.getId();
		}
	}

	/**
	 * @see org.gbif.portal.dao.RawOccurrenceRecordDAO#getUniqueRecord(long,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public RawOccurrenceRecord getUniqueRecord(long dataResourceId,
			String institutionCode, String collectionCode,
			String catalogueNumber, String unitQualifier) {
		StringBuffer query = new StringBuffer(
				RawOccurrenceRecordDAOImpl.QUERY_UNIQUE_SQL);
		List<Object> params = new LinkedList<Object>();
		params.add(new Long(dataResourceId));
		if (StringUtils.isNotEmpty(institutionCode)) {
			query.append(" and institution_code=?");
			params.add(institutionCode);
		}
		if (StringUtils.isNotEmpty(collectionCode)) {
			query.append(" and collection_code=?");
			params.add(collectionCode);
		}
		if (StringUtils.isNotEmpty(catalogueNumber)) {
			query.append(" and catalogue_number=?");
			params.add(catalogueNumber);
		}
		if (StringUtils.isNotEmpty(unitQualifier)) {
			query.append(" and unit_qualifier=?");
			params.add(unitQualifier);
		}

		List<RawOccurrenceRecord> results = (List<RawOccurrenceRecord>) getJdbcTemplate()
				.query(query.toString(),
						params.toArray(),
						new RowMapperResultSetExtractor(
								rawOccurrenceRecordRowMapper, 1));
		if (results.size() == 0) {
			return null;
		} else if (results.size() > 1) {
			logger.warn("Found multiple RawOccurrenceRecords with dataResourceId["
					+ dataResourceId
					+ "], institutionCode["
					+ institutionCode
					+ "], collectionCode["
					+ collectionCode
					+ "] and catalogueNumber[" + catalogueNumber + "]");
		}
		return results.get(0);
	}

	/**
	 * @see org.gbif.portal.dao.RawOccurrenceRecordDAO#getCreatedOrModifiedSince(java.util.Date)
	 */
	@SuppressWarnings("unchecked")
	public List<RawOccurrenceRecord> getCreatedOrModifiedSince(
			final long dataResourceId, final java.util.Date start,
			long startAt, int maxResults) {
		logger.info("Getting records last harvested since[" + start
				+ "] for DR ID:" + dataResourceId);
		final Timestamp ts = new Timestamp(start.getTime());
		List<RawOccurrenceRecord> results = (List<RawOccurrenceRecord>) getJdbcTemplate()
				.query(RawOccurrenceRecordDAOImpl.QUERY_MODIFIED_SINCE,
						new Object[] { dataResourceId, ts, startAt, maxResults },
						new int[] { Types.INTEGER, Types.TIMESTAMP,
								Types.INTEGER, Types.INTEGER },
						new RowMapperResultSetExtractor(
								rawOccurrenceRecordRowMapper));
		return results;
	}

	/**
	 * @see org.gbif.portal.dao.RawOccurrenceRecordDAO#getById(long)
	 */
	@SuppressWarnings("unchecked")
	public RawOccurrenceRecord getById(final long id) {
		List<RawOccurrenceRecord> results = (List<RawOccurrenceRecord>) getJdbcTemplate()
				.query(RawOccurrenceRecordDAOImpl.QUERY_ID_SQL,
						new Object[] { id },
						new RowMapperResultSetExtractor(
								rawOccurrenceRecordRowMapper, 1));
		if (results.size() == 0) {
			return null;
		} else if (results.size() > 1) {
			logger.warn("Found multiple RawOccurrenceRecords with Id[" + id
					+ "]");
		}
		return results.get(0);
	}

	/**
	 * @see org.gbif.portal.dao.RawOccurrenceRecordDAO#getDataResourceIdsFor(long)
	 */
	@SuppressWarnings("unchecked")
	public List<Long> getDataResourceIdsFor(long resourceAccessPointId) {
		List<Long> results = (List<Long>) getJdbcTemplate().query(
				RawOccurrenceRecordDAOImpl.QUERY_DATA_RESOURCE,
				new Object[] { resourceAccessPointId },
				new RowMapperResultSetExtractor(longRowMapper, 3));
		return results;
	}

    /**
     * @see org.ala.biocache.dao.RawOccurrenceRecordDAO#deleteById(long) 
     */
    public void deleteById(final long id) {
        getJdbcTemplate().update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                       PreparedStatement ps = conn.prepareStatement(DELETE_SQL);
                       ps.setLong(1, id);
                       return ps;
                }
            }
        );
    }

	/**
	 * @return Returns the rawOccurrenceRecordRowMapper.
	 */
	public RawOccurrenceRecordRowMapper getRawOccurrenceRecordRowMapper() {
		return rawOccurrenceRecordRowMapper;
	}

	/**
	 * @param rawOccurrenceRecordRowMapper
	 *            The rawOccurrenceRecordRowMapper to set.
	 */
	public void setRawOccurrenceRecordRowMapper(
			RawOccurrenceRecordRowMapper rawOccurrenceRecordRowMapper) {
		this.rawOccurrenceRecordRowMapper = rawOccurrenceRecordRowMapper;
	}

	private java.sql.Date createSQLDate(Date date) {
		java.sql.Date sqlDate = null;

		if (date != null) {
			sqlDate = new java.sql.Date(date.getTime());
		}

		return sqlDate;
	}
}
