export default function LoadingSpinner() {
  return (
    <div className="flex justify-center items-center h-full w-full py-10">
      <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-[#8B5CF6]"></div>
    </div>
  );
}
