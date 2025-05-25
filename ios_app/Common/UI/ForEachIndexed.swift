import SwiftUI

func ForEachIndexed<T, Content: View>(
    _ items: Array<T>,
    @ViewBuilder content: @escaping (Int, T) -> Content
) -> some View {
    ForEach(Array(items.enumerated()), id: \.offset) { index, item in
        content(index, item)
    }
}
