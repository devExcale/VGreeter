package ovh.excale.fainabot.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ovh.excale.fainabot.models.Track;
import ovh.excale.fainabot.repositories.TrackRepository;

@Service
public class TrackService {

	private final TrackRepository trackRepo;

	public TrackService(TrackRepository trackRepo) {
		this.trackRepo = trackRepo;
	}

	public Track randomTrack() {

		long qty = trackRepo.count();
		int idx = (int) (Math.random() * qty);

		Page<Track> trackPage = trackRepo.findAll(PageRequest.of(idx, 1));
		Track track = null;
		if(trackPage.hasContent())
			track = trackPage.getContent()
					.get(0);

		return track;
	}

}
