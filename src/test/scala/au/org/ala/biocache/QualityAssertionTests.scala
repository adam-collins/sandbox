package au.org.ala.biocache

import org.scalatest.FunSuite
import org.apache.cassandra.thrift.ConsistencyLevel
import org.wyki.cassandra.pelops.{Policy, Mutator, Pelops}

class QualityAssertionTests extends FunSuite {

  test("Add and delete user systemAssertions"){

    val uuid = "test-uuid-qa-delete1"
    try {
        Pelops.addPool("test", Array("localhost"), 9160, false, "occ", new Policy)
        val mutator = Pelops.createMutator("test","occ")
        mutator.deleteColumns(uuid, "occ", "userQualityAssertion","qualityAssertion")
        mutator.execute(ConsistencyLevel.ONE)
    } catch {
        case e: Exception => e.printStackTrace
    }

    val qa1 = QualityAssertion(AssertionCodes.COORDINATE_HABITAT_MISMATCH, true)
    OccurrenceDAO.addUserAssertion(uuid, qa1)

    val qa2 = QualityAssertion(AssertionCodes.COORDINATE_HABITAT_MISMATCH, false)
    OccurrenceDAO.addUserAssertion(uuid, qa2)

    expect(2){
        val userAssertions = OccurrenceDAO.getUserAssertions(uuid)
        userAssertions.size
    }

    //run the delete
    OccurrenceDAO.deleteUserAssertion(uuid, qa2.uuid)
    val userAssertions = OccurrenceDAO.getUserAssertions(uuid)
    expect(1){ userAssertions.size }

    //retrieve the record and check assertion is set and kosher values
    val fullRecord = OccurrenceDAO.getByUuid("test-uuid-qa-delete1").get
    val found = fullRecord.assertions.find(ass => { ass equals AssertionCodes.COORDINATE_HABITAT_MISMATCH.name  })
    expect(AssertionCodes.COORDINATE_HABITAT_MISMATCH.name){ found.get }
    expect(false){ fullRecord.geospatiallyKosher }

    Pelops.shutdown
  }
}