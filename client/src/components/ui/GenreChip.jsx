export default function GenreChip({ genre }) {
  return (
    <span className="px-2 py-1 text-xs font-medium border border-[#8B5CF6] text-gray-300 rounded-full hover:bg-[#8B5CF6] hover:text-white transition-colors cursor-default">
      {genre}
    </span>
  );
}
