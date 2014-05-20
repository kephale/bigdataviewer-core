package bdv.img.remote;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.TimePoint;
import bdv.img.hdf5.Hdf5ImageLoader;
import bdv.img.hdf5.Hdf5ImageLoader.DimsAndExistence;
import bdv.img.hdf5.Hdf5ImageLoader.MipmapInfo;
import bdv.img.hdf5.ViewLevelId;

public class RemoteImageLoaderMetaData
{
	protected int maxNumTimepoints;

	protected int maxNumSetups;

	protected int maxNumLevels;

	// TODO clean up after spim_data switch
	protected final HashMap< Integer, MipmapInfo > perSetupMipmapInfo;

	/**
	 * Maps {@link ViewLevelId} (timepoint, setup, level) to
	 * {@link DimsAndExistence}. Every entry represents the existence and
	 * dimensions of one image.
	 */
	protected final HashMap< ViewLevelId, DimsAndExistence > dimsAndExistence;

	public RemoteImageLoaderMetaData( final Hdf5ImageLoader imgLoader, final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
	{
		perSetupMipmapInfo = new HashMap< Integer, MipmapInfo >();
		dimsAndExistence = new HashMap< ViewLevelId, DimsAndExistence >();

		final List< TimePoint > timepoints = sequenceDescription.getTimePoints().getTimePointsOrdered();
		maxNumTimepoints = timepoints.get( timepoints.size() - 1 ).getId() + 1;

		final List< ? extends BasicViewSetup > setups = sequenceDescription.getViewSetupsOrdered();
		maxNumSetups = setups.get( setups.size() - 1 ).getId() + 1;

		maxNumLevels = 0;
		for ( final BasicViewSetup setup : setups )
		{
			final int setupId = setup.getId();
			final MipmapInfo info = imgLoader.getMipmapInfo( setupId );
			perSetupMipmapInfo.put( setupId, info );

			final int numLevels = info.getNumLevels();
			if ( numLevels > maxNumLevels )
				maxNumLevels = numLevels;

			for ( final TimePoint timepoint : timepoints )
			{
				final int timepointId = timepoint.getId();
				for ( int level = 0; level < numLevels; ++level )
				{
					final ViewLevelId id = new ViewLevelId( timepointId, setupId, level );
					dimsAndExistence.put( id, imgLoader.getDimsAndExistence( id ) );
				}
			}
		}

	}

	/**
	 * Create an map from {@link ViewLevelId} (timepoint, setup, level) to
	 * int[]. Every entry is the dimensions in cells (instead of pixels) of one
	 * image.
	 */
	protected HashMap< ViewLevelId, int[] > createCellsDimensions()
	{
		final HashMap< ViewLevelId, int[] > cellsDimensions = new HashMap< ViewLevelId, int[] >();
		for ( final Entry< ViewLevelId, DimsAndExistence > entry : dimsAndExistence.entrySet() )
		{
			final ViewLevelId id = entry.getKey();
			final long[] imageDimensions = entry.getValue().getDimensions();
			final int[] cellSize = perSetupMipmapInfo.get( id.getViewSetupId() ).getSubdivisions()[ id.getLevel() ];
			final int[] dims = new int[] {
					( int ) imageDimensions[ 0 ] / cellSize[ 0 ],
					( int ) imageDimensions[ 1 ] / cellSize[ 1 ],
					( int ) imageDimensions[ 2 ] / cellSize[ 2 ] };
			cellsDimensions.put( id, dims );
		}
		return cellsDimensions;
	}
}
