export default function SkeletonCard() {
  return (
    <div className="w-full aspect-[2/3] bg-[#1a1a2e] rounded-lg animate-pulse overflow-hidden relative">
      <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent"></div>
      <div className="absolute bottom-3 left-3 right-3 space-y-2">
        <div className="h-4 bg-[#2D2D44] rounded w-3/4"></div>
        <div className="h-3 bg-[#2D2D44] rounded w-1/2"></div>
      </div>
    </div>
  );
}
